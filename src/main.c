#include <errno.h>

#include <zephyr/kernel.h>
#include <zephyr/types.h>
#include <zephyr/logging/log.h>

#include "drivers/led/led.h"
#include "drivers/sb1602/sb1602b.h"

#include "app/app.h"

LOG_MODULE_REGISTER(main, CONFIG_USER_MAIN_LOG_LEVEL);

int main(void)
{
    int ret;

    ret = led_init();
    if (ret != 0) {
        LOG_ERR("fail init: LED");
        __ASSERT(0, "fail init: LED");
    }
    ret = sb1602b_init();
    if (ret != 0) {
        LOG_ERR("fail init: SB1602");
        __ASSERT(0, "fail init: SB1602");
    }
    ret = app_init();
    if (ret != 0) {
        LOG_ERR("fail init: app_init(ret=%d)", ret);
        __ASSERT(0, "fail init: ble ctrl");
    }
    // no return
    app_start();
}
