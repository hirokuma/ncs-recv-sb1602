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

#include "app_internal.h"
#include "lps/lps.h"

LOG_MODULE_DECLARE(app, CONFIG_USER_APP_LOG_LEVEL);

#define DEVICE_NAME             CONFIG_BT_DEVICE_NAME
#define DEVICE_NAME_LEN         (sizeof(DEVICE_NAME) - 1)


static const struct bt_data ad[] = {
    BT_DATA_BYTES(BT_DATA_FLAGS, (BT_LE_AD_GENERAL | BT_LE_AD_NO_BREDR)),
    BT_DATA(BT_DATA_NAME_COMPLETE, DEVICE_NAME, DEVICE_NAME_LEN),
};

static const struct bt_data sd[] = {
    BT_DATA_BYTES(BT_DATA_UUID128_ALL, UUID_LPS_VAL),
};

static void connected(struct bt_conn *conn, uint8_t err)
{
    if (err) {
        LOG_ERR("connected: Connection failed (err %u)", err);
        return;
    }

    LOG_DBG("connected: Connected");
}

static void disconnected(struct bt_conn *conn, uint8_t reason)
{
    LOG_DBG("Disconnected (reason %u)", reason);
}

BT_CONN_CB_DEFINE(conn_callbacks) = {
    .connected        = connected,
    .disconnected     = disconnected,
};

int ble_adv_start(void)
{
    int err;

    err = bt_le_adv_start(BT_LE_ADV_CONN, 
                    ad, ARRAY_SIZE(ad),
                    sd, ARRAY_SIZE(sd));
    if (err) {
        LOG_ERR("ble_adv_start: fail bt_le_adv_start(err=%d)", err);
        return -1;
    }

    LOG_DBG("ble_adv_start: done.");
    return 0;
}
