#include <stddef.h>
#include <string.h>
#include <errno.h>

#include <zephyr/kernel.h>
#include <zephyr/types.h>
#include <zephyr/logging/log.h>

#include <zephyr/bluetooth/bluetooth.h>
#include <zephyr/bluetooth/hci.h>
#include <zephyr/bluetooth/conn.h>
#include <zephyr/bluetooth/uuid.h>
#include <zephyr/bluetooth/gatt.h>

#include <bluetooth/services/lbs.h>

#include "drivers/button/button.h"
#include "drivers/led/led.h"

#include "app_internal.h"

LOG_MODULE_DECLARE(app, CONFIG_USER_APP_LOG_LEVEL);

static bool app_button_state;

static void button_pressed(int target, bool pressed)
{
    LOG_DBG("button_pressed: %d", pressed);
    app_button_state = pressed;
    bt_lbs_send_button_state(app_button_state);
}


/* STEP 8.2 - Define the application callback function for controlling the LED */
static void app_led_cb(bool led_state)
{
    led_set(2, led_state);
}

/* STEP 9.2 - Define the application callback function for reading the state of the button */
static bool app_button_cb(void)
{
    return app_button_state;
}

int ble_lbs_init(void)
{
    int ret;

    struct bt_lbs_cb cb = {
        .led_cb = app_led_cb,
        .button_cb = app_button_cb,
    };
    ret = bt_lbs_init(&cb);

    button_set_notify(button_pressed);

    LOG_DBG("ble_lbs_init: done.");
    return ret;
}
