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

#include "app.h"
#include "app_internal.h"

LOG_MODULE_REGISTER(app, CONFIG_USER_APP_LOG_LEVEL);

int app_init(void)
{
    int ret;

    ret = bt_enable(NULL);
    if (ret != 0) {
        LOG_ERR("app_init: bt_enable(ret=%d)", ret);
        return -1;
    }
    ret = ble_lps_init();
    if (ret != 0) {
        LOG_ERR("app_init: ble_lps_init(ret=%d)", ret);
        return -1;
    }

    LOG_INF("app_init done.");
    return 0;
}

_Noreturn void app_start(void)
{
    int ret;

    ret = ble_adv_start();
    if (ret != 0) {
        LOG_ERR("app_start: ble_adv_start(ret=%d)", ret);
        __ASSERT(0, "app_start: ble_adv_start");
    }

    k_sleep(K_FOREVER);
}
