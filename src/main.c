#include <errno.h>

#include <zephyr/kernel.h>
#include <zephyr/types.h>
#include <zephyr/logging/log.h>

#include <zephyr/bluetooth/bluetooth.h>
#include <zephyr/usb/usb_device.h>

#include "drivers/button/button.h"
#include "drivers/led/led.h"
#include "drivers/sb1602/sb1602b.h"

#include "app/app.h"

LOG_MODULE_REGISTER(main, CONFIG_USER_MAIN_LOG_LEVEL);

int main(void)
{
    int ret;

    ret = led_init();
    if (ret != 0) {
        LOG_ERR("main: led_init(ret=%d)", ret);
        __ASSERT(0, "fail init: LED");
    }
    if (IS_ENABLED(CONFIG_USB_DEVICE_STACK)) {
        ret = usb_enable(NULL);
        if (ret != 0) {
            LOG_ERR("main: usb_enable(ret=%d)", ret);
            __ASSERT(0, "fail init: USB");
        }
    }

    ret = button_init();
    if (ret != 0) {
        LOG_ERR("main: button_init(ret=%d)", ret);
        __ASSERT(0, "fail init: Button");
    }
    ret = sb1602b_init();
    if (ret != 0) {
        LOG_ERR("main: sb1602b_init(ret=%d)", ret);
        __ASSERT(0, "fail init: SB1602");
    }
    ret = bt_enable(NULL);
    if (ret != 0) {
        LOG_ERR("main: bt_enable(ret=%d)", ret);
        __ASSERT(0, "fail init: ble enable");
    }

    ret = app_init();
    if (ret != 0) {
        LOG_ERR("main: app_init(ret=%d)", ret);
        __ASSERT(0, "fail init: ble ctrl");
    }
    // no return
    app_start();
}
