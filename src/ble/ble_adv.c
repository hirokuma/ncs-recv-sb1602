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

#include "ble.h"
#include "lps/lps.h"

LOG_MODULE_DECLARE(app, CONFIG_USER_BLE_LOG_LEVEL);

#define DEVICE_NAME             CONFIG_BT_DEVICE_NAME
#define DEVICE_NAME_LEN         (sizeof(DEVICE_NAME) - 1)

static struct k_work adv_work;

static const struct bt_data ad[] = {
    BT_DATA_BYTES(BT_DATA_FLAGS, (BT_LE_AD_GENERAL | BT_LE_AD_NO_BREDR)),
    BT_DATA(BT_DATA_NAME_COMPLETE, DEVICE_NAME, DEVICE_NAME_LEN),
};

static const struct bt_data sd[] = {
    BT_DATA_BYTES(BT_DATA_UUID128_ALL, UUID_LPS_VAL),
};

static struct ble_conn_cb ble_conn_cb;

static void connected(struct bt_conn *conn, uint8_t err)
{
    if (err) {
        LOG_ERR("connected: Connection failed (err %u)", err);
        return;
    }

    if (ble_conn_cb.conn_cb) {
        ble_conn_cb.conn_cb();
    }
    LOG_DBG("connected");
}

static void disconnected(struct bt_conn *conn, uint8_t reason)
{
    if (ble_conn_cb.disconn_cb) {
        ble_conn_cb.disconn_cb(reason);
    }
    LOG_DBG("disconnected");
}

// Resume advertising after a disconnection
static void adv_work_handler(struct k_work *work)
{
    (void)work;

    int err = bt_le_adv_start(BT_LE_ADV_CONN_FAST_2,
                              ad, ARRAY_SIZE(ad),
                              sd, ARRAY_SIZE(sd));
    if (err) {
        LOG_ERR("ble_adv_start: fail bt_le_adv_start(err=%d)", err);
        return;
    }

    LOG_DBG("advertising started");
}

static void advertising_start(void)
{
    k_work_submit(&adv_work);
}

static void recycled(void)
{
    LOG_DBG("Connection object available from previous conn. Disconnect is complete!");
    advertising_start();
}

BT_CONN_CB_DEFINE(conn_callbacks) = {
    .connected        = connected,
    .disconnected     = disconnected,
    .recycled         = recycled,
};

int ble_adv_start(const struct ble_conn_cb *callbacks)
{
    if (callbacks) {
        ble_conn_cb = *callbacks;
    } else {
        ble_conn_cb.conn_cb = NULL;
        ble_conn_cb.disconn_cb = NULL;
    }

    k_work_init(&adv_work, adv_work_handler);
    advertising_start();

    return 0;
}
