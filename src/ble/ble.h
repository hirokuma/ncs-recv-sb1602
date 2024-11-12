#ifndef BLE_H
#define BLE_H

typedef void (*ble_connected_cb_t)(void);
typedef void (*ble_disconnected_cb_t)(uint8_t reason);

struct ble_conn_cb {
    ble_connected_cb_t conn_cb;
    ble_disconnected_cb_t disconn_cb;
};

int ble_adv_start(const struct ble_conn_cb *callbacks);

#endif /* BLE_H */
