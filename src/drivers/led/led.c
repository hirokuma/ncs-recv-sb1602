#include <zephyr/kernel.h>
#include <zephyr/logging/log.h>

#include <zephyr/drivers/gpio.h>

#include "led.h"

LOG_MODULE_REGISTER(driver_led, CONFIG_USER_DRV_LED_LOG_LEVEL);

static const struct gpio_dt_spec dev_leds[] = {
    GPIO_DT_SPEC_GET(DT_ALIAS(led0), gpios),
    GPIO_DT_SPEC_GET(DT_ALIAS(led1), gpios),
    GPIO_DT_SPEC_GET(DT_ALIAS(led2), gpios),
};

int led_init(void)
{
    for (int n = 0; n < ARRAY_SIZE(dev_leds); n++) {
        if (!device_is_ready(dev_leds[n].port)) {
            LOG_DBG("fail led_init(n=%d): device_is_ready", n);
            return -ENXIO;
        }

        int ret = gpio_pin_configure_dt(&dev_leds[n], GPIO_OUTPUT_INACTIVE);
        if (ret < 0) {
            LOG_DBG("fail led_init(n=%d): gpio_pin_configure_dt(ret=%d)", n, ret);
            return -EIO;
        }
    }

    LOG_INF("led_init: done.");
    return 0;
}

void led_set(int target, int onoff)
{
    if (target < 0 || target >= ARRAY_SIZE(dev_leds)) {
        LOG_ERR("fail led_set: out of target=%d", target);
        return;
    }
    gpio_pin_set_dt(&dev_leds[target], onoff);
}

void led_blink(int target)
{
    if (target < 0 || target >= ARRAY_SIZE(dev_leds)) {
        LOG_ERR("fail led_blink: out of target=%d", target);
        return;
    }
    gpio_pin_toggle_dt(&dev_leds[target]);
}
