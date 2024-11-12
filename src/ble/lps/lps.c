/**
 * @file lps.c
 * @brief LCD Print Service
 */

#include <stddef.h>
#include <string.h>
#include <errno.h>

#include <zephyr/types.h>
#include <zephyr/sys/printk.h>
#include <zephyr/sys/byteorder.h>
#include <zephyr/kernel.h>
#include <zephyr/logging/log.h>
#include <zephyr/bluetooth/bluetooth.h>
#include <zephyr/bluetooth/hci.h>
#include <zephyr/bluetooth/conn.h>
#include <zephyr/bluetooth/uuid.h>
#include <zephyr/bluetooth/gatt.h>

#include "lps.h"

LOG_MODULE_REGISTER(LPS_Service, LOG_LEVEL_DBG);

/*
 * UUID
 */

#define UUID_LPS BT_UUID_DECLARE_128(UUID_LPS_VAL)

/// @brief PRINT Characteristic UUID
#define UUID_LPS_PRINT_VAL \
    BT_UUID_128_ENCODE(0xa00c1711, 0x74ff, 0x4bd5, 0x9e86, 0xcf601d80c054)
#define UUID_LPS_PRINT BT_UUID_DECLARE_128(UUID_LPS_PRINT_VAL)

/// @brief CLEAR Characteristic UUID
#define UUID_LPS_CLEAR_VAL \
    BT_UUID_128_ENCODE(0xa00c1712, 0x74ff, 0x4bd5, 0x9e86, 0xcf601d80c054)
#define UUID_LPS_CLEAR BT_UUID_DECLARE_128(UUID_LPS_CLEAR_VAL)




/// @brief service callbacks
static struct lps_cb lps_cb;


/**
 * Callback application function triggered by writing to PRINT Characteristic.
 */
static ssize_t write_print(
    struct bt_conn *conn,
    const struct bt_gatt_attr *attr,
    const void *buf,
    uint16_t len,
    uint16_t offset,
    uint8_t flags)
{
	LOG_DBG("Attribute write print, handle: %u, conn: %p", attr->handle, (const void *)conn);



    // TODO: Modify callback
    if (lps_cb.print_write_cb) {
        int ret = lps_cb.print_write_cb(buf, len, offset);
        if (ret != 0) {
            LOG_ERR("Write print: callback error happen: %d", ret);
            return BT_GATT_ERR(BT_ATT_ERR_VALUE_NOT_ALLOWED);
        }
    }

    return len;
}

/**
 * Callback application function triggered by writing to CLEAR Characteristic.
 */
static ssize_t write_clear(
    struct bt_conn *conn,
    const struct bt_gatt_attr *attr,
    const void *buf,
    uint16_t len,
    uint16_t offset,
    uint8_t flags)
{
	LOG_DBG("Attribute write clear, handle: %u, conn: %p", attr->handle, (const void *)conn);

    // TODO: Check length
    if (len != 1) {
        LOG_ERR("Write clear: Incorrect data length(%u)", len);
        return BT_GATT_ERR(BT_ATT_ERR_INVALID_ATTRIBUTE_LEN);
    }

    // TODO: Check offset
    if (offset != 0) {
        LOG_ERR("Write clear: Incorrect data offset(%u)", offset);
        return BT_GATT_ERR(BT_ATT_ERR_INVALID_OFFSET);
	}

    // TODO: Modify callback
    if (lps_cb.clear_write_cb) {
        int ret = lps_cb.clear_write_cb(buf, len, offset);
        if (ret != 0) {
            LOG_ERR("Write clear: callback error happen: %d", ret);
            return BT_GATT_ERR(BT_ATT_ERR_VALUE_NOT_ALLOWED);
        }
    }

    return len;
}

// LPS Service Declaration
BT_GATT_SERVICE_DEFINE(
    lps_svc,
    BT_GATT_PRIMARY_SERVICE(UUID_LPS),

    // PRINT Characteristic
    BT_GATT_CHARACTERISTIC(
        // UUID
        UUID_LPS_PRINT,
        // Properties
        BT_GATT_CHRC_WRITE,
        // Permissions
        BT_GATT_PERM_WRITE,
        // Characteristic Attribute read callback
        NULL,
        // Characteristic Attribute write callback
        write_print,
        // Characteristic Attribute user data(TODO: modify)
        NULL
    ),

    // CLEAR Characteristic
    BT_GATT_CHARACTERISTIC(
        // UUID
        UUID_LPS_CLEAR,
        // Properties
        BT_GATT_CHRC_WRITE,
        // Permissions
        BT_GATT_PERM_WRITE,
        // Characteristic Attribute read callback
        NULL,
        // Characteristic Attribute write callback
        write_clear,
        // Characteristic Attribute user data(TODO: modify)
        NULL
    ),
);


/*
 * Functions
 */

int lps_init(const struct lps_cb *callbacks)
{
    lps_cb = *callbacks;

    // TODO: add your code

    return 0;
}

