#include <zephyr/kernel.h>
#include <zephyr/logging/log.h>

#include <zephyr/drivers/gpio.h>

#include "button.h"

LOG_MODULE_REGISTER(driver_button, CONFIG_USER_DRV_BUTTON_LOG_LEVEL);

#define CHATTER_NOTARGET        (-1)
#define CHATTER_CONFIRM_COUNT   (5)
#define CHATTER_GET_MSEC        (10)
#define CHATTER_SAFETY_COUNT    (100)

static const struct gpio_dt_spec dev_buttons[] = {
    GPIO_DT_SPEC_GET(DT_ALIAS(sw0), gpios),
};

static button_pressed_cb_t button_cb;
static struct gpio_callback gpio_cb;

#define STACKSIZE 1024
#define PRIORITY 7
static K_SEM_DEFINE(chatter_ok, 0, 1);
static int target_chatter = CHATTER_NOTARGET;

static void button_pressed(const struct device *dev, struct gpio_callback *cb, uint32_t pins)
{
    if (target_chatter == CHATTER_NOTARGET) {
        for (int n = 0; n < ARRAY_SIZE(dev_buttons); n++) {
            if (pins & BIT(dev_buttons[n].pin)) {
                target_chatter = n;
                LOG_DBG("button_pressed: target=%d", target_chatter);
                break;
            }
        }
        k_sem_give(&chatter_ok);
    }
}

int button_init(void)
{
    int ret;
    uint32_t pin_mask = 0;

    for (int n = 0; n < ARRAY_SIZE(dev_buttons); n++) {
        if (!device_is_ready(dev_buttons[n].port)) {
            LOG_DBG("fail button_init(n=%d): device_is_ready", n);
            return -ENXIO;
        }

        ret = gpio_pin_configure_dt(&dev_buttons[n], GPIO_INPUT);
        if (ret < 0) {
            LOG_DBG("fail button_init(n=%d): gpio_pin_configure_dt(ret=%d)", n, ret);
            return ret;
        }
        // ACTIVEのみ拾う
        ret = gpio_pin_interrupt_configure_dt(&dev_buttons[n], GPIO_INT_EDGE_BOTH);
        if (ret < 0) {
            LOG_DBG("fail button_init(n=%d): gpio_pin_interrupt_configure_dt(ret=%d)", n, ret);
            return ret;
        }
        pin_mask |= BIT(dev_buttons[n].pin);
    }
    gpio_init_callback(&gpio_cb, button_pressed, pin_mask);
    for (int n = 0; n < ARRAY_SIZE(dev_buttons); n++) {
        ret = gpio_add_callback(dev_buttons[n].port, &gpio_cb);
        if (ret < 0) {
            LOG_DBG("fail button_init(n=%d): gpio_add_callback(ret=%d)", n, ret);
            return ret;
        }
    }

    LOG_INF("button_init: done.");
    return 0;
}

void button_set_notify(button_pressed_cb_t cb)
{
    button_cb = cb;
}

bool button_read(int target)
{
    if (target < 0 || target >= ARRAY_SIZE(dev_buttons)) {
        LOG_ERR("fail button_read: out of target=%d", target);
        return false;
    }
    return gpio_pin_get_dt(&dev_buttons[target]);
}

static void button_chatter_thread(void)
{
    int safety_count = CHATTER_SAFETY_COUNT;
    while (safety_count--) {
        k_sem_take(&chatter_ok, K_FOREVER);

        while (1) {
            bool init_val = gpio_pin_get_dt(&dev_buttons[target_chatter]);
            LOG_DBG("button_chatter_thread: start init_val=%d", init_val);
            int count = 0;
            while (count < CHATTER_CONFIRM_COUNT) {
                k_msleep(CHATTER_GET_MSEC);
                bool val = gpio_pin_get_dt(&dev_buttons[target_chatter]);
                if (val == init_val) {
                    count++;
                } else {
                    LOG_DBG("button_chatter_thread: not same=%d", val);
                    init_val = val;
                    count = 0;
                }
            }
            if (count != CHATTER_CONFIRM_COUNT) {
                break;
            }
            LOG_DBG("pressed!");
            if (button_cb) {
                button_cb(target_chatter, init_val);
            }
            break;
        }

        k_sem_reset(&chatter_ok);
        target_chatter = CHATTER_NOTARGET;
        LOG_DBG("button_chatter_thread: after k_sem_reset");
    }
}

static K_THREAD_DEFINE(button_chatter_thread_id, STACKSIZE, button_chatter_thread, NULL, NULL,
        NULL, PRIORITY, 0, 0);
