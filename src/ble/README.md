# BLE Services

This directory contains the BLE service implementations.

## LCD Print Service (LPS)

A custom service for controlling LCD display remotely via BLE.

### Service UUID

- Service UUID: `a00c1710-74ff-4bd5-9e86-cf601d80c054`

### Characteristics

#### PRINT Characteristic

- UUID: `a00c1711-74ff-4bd5-9e86-cf601d80c054`
- Properties: Write
- Description: Write text to display on LCD

#### CLEAR Characteristic

- UUID: `a00c1712-74ff-4bd5-9e86-cf601d80c054`
- Properties: Write
- Description: Clear LCD screen
- Restrictions:
  - Length must be 1 byte
  - Offset must be 0

## Usage

1. Initialize BLE advertising:

```c
struct ble_conn_cb callbacks = {
    .conn_cb = on_connected,
    .disconn_cb = on_disconnected
};
ble_adv_start(&callbacks);
```

2. Initialize LCD Print Service:

```c
struct lps_cb lps_callbacks = {
    .print_write_cb = on_print,
    .clear_write_cb = on_clear
};
lps_init(&lps_callbacks);
```

## Directory Structure

```
ble/
├── README.md
├── ble.h           - BLE core interface
├── ble_adv.c       - BLE advertising implementation
└── lps/            - LCD Print Service
    ├── lps.h       - Service interface
    ├── lps.c       - Service implementation
    └── config.json - Service configuration
```
