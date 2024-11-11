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

#include "drivers/sb1602/sb1602b.h"

#include "app_internal.h"
#include "lps/lps.h"

LOG_MODULE_DECLARE(app, CONFIG_USER_APP_LOG_LEVEL);

static int line_y = 0;

static int write_cb(const void *data, uint16_t len, uint16_t offset)
{
    LOG_DBG("ble_lsp: write_cb: len=%d, offset=%d", len, offset);
    if ((len == 0) || (offset != 0)) {
        LOG_ERR("ble_lsp: write_cb: invalid param: len=%d, offset=%d", len, offset);
        return -EINVAL;
    }
    const char *p = (const char *)data;
    sb1602b_move(0, line_y);
    sb1602b_output(p, len);
    if (line_y == 0) {
        line_y = 1;
    } else {
        line_y = 0;
    }
    return 0;
}

static int clear_cb(const void *data, uint16_t len, uint16_t offset)
{
    LOG_DBG("ble_lsp: clear_cb: len=%d, offset=%d", len, offset);
    if ((len != 1) || (offset != 0)) {
        LOG_ERR("ble_lsp: clear_cb: invalid param: len=%d, offset=%d", len, offset);
        return -EINVAL;
    }
    const uint8_t *p = (const uint8_t *)data;
    LOG_DBG(__FILE__ "ble_lsp: clear_cb: data=%02x", *p);
    sb1602b_clear();
    line_y = 0;
    return ;
}

int ble_lps_init(void)
{
    int ret;

    const struct lps_cb cb = {
        .print_write_cb = write_cb,
        .clear_write_cb = clear_cb,
    };
    ret = lps_init(&cb);
    if (ret) {
        LOG_ERR("ble_lps_init: lps_init(ret=%d)", ret);
        return -1;
    }

    LOG_DBG("ble_lps_init: done.");
    return 0;
}
