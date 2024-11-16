#include <stddef.h>
#include <string.h>
#include <errno.h>

#include <zephyr/kernel.h>
#include <zephyr/types.h>
#include <zephyr/logging/log.h>

#include "ble/ble.h"
#include "drivers/led/led.h"
#include "drivers/sb1602/sb1602b.h"

#include "app.h"
#include "app_internal.h"

LOG_MODULE_REGISTER(app, CONFIG_USER_APP_LOG_LEVEL);

int app_init(void)
{
    int ret;

    ret = ble_lps_init();
    if (ret != 0) {
        LOG_ERR("app_init: ble_lps_init(ret=%d)", ret);
        return -1;
    }
    ret = ble_lbs_init();
    if (ret != 0) {
        LOG_ERR("app_init: ble_lbs_init(ret=%d)", ret);
        return -1;
    }

    LOG_INF("app_init done.");
    return 0;
}

static void ble_connected(void)
{
    LOG_DBG("app: connected: Connected");
    led_set(1, 1);
}

static void ble_disconnected(uint8_t reason)
{
    LOG_DBG("app: Disconnected (reason %u)", reason);
    led_set(1, 0);
}

FUNC_NORETURN void app_start(void)
{
    int ret;

    const struct ble_conn_cb cb = { ble_connected, ble_disconnected };
    ret = ble_adv_start(&cb);
    if (ret != 0) {
        LOG_ERR("app_start: ble_adv_start(ret=%d)", ret);
        __ASSERT(0, "app_start: ble_adv_start");
    }

    k_sleep(K_FOREVER);
}
