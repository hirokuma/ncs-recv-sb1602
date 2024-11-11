/**
 * @file lps.h
 * @brief LCD Print Service
 */

#ifndef LPS_H_
#define LPS_H_

#ifdef __cplusplus
extern "C" {
#endif // __cplusplus

#include <stdint.h>

#include <zephyr/bluetooth/uuid.h>

/*
 * UUID
 */

/// @brief LPS Service UUID
#define UUID_LPS_VAL \
    BT_UUID_128_ENCODE(0xa00c1710, 0x74ff, 0x4bd5, 0x9e86, 0xcf601d80c054)


/*
 * Types
 */

/// @brief Write callback type for PRINT Characteristic.
// TODO: Modifying parameters
typedef int (*lps_print_write_cb_t)(const void *data, uint16_t len, uint16_t offset);

/// @brief Write callback type for CLEAR Characteristic.
// TODO: Modifying parameters
typedef int (*lps_clear_write_cb_t)(const void *data, uint16_t len, uint16_t offset);


/// @brief Callback struct used by the LPS Service.
struct lps_cb {
    lps_print_write_cb_t print_write_cb;
    lps_clear_write_cb_t clear_write_cb;
};


/*
 * Functions
 */

/// @brief Initialize the LPS Service.
int lps_init(const struct lps_cb *callbacks);


#ifdef __cplusplus
}
#endif // __cplusplus

#endif // LPS_H_
