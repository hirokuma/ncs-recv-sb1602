# Drivers Documentation

This directory contains device drivers for the NCS (nRF Connect SDK) project.

## LCD Driver (SB1602)

The SB1602 is a 16x2 character LCD module with I2C interface. This driver provides functionality to control the display.

### Features

- 16x2 character display
- I2C interface support
- 8-bit parallel data interface
- 5x8 dots character font
- Low power consumption

### Usage

To use the SB1602 LCD driver in your project:

1. Include the driver header:
```c
#include "sb1602.h"
```

2. Initialize the I2C interface and the LCD:
```c
struct sb1602_config config = {
    .i2c_dev = DEVICE_DT_GET(DT_NODELABEL(i2c0)),
    .i2c_addr = 0x3E
};

sb1602_init(&config);
```

3. Write text to the display:
```c
sb1602_clear();
sb1602_write_string("Hello, World!");
```

### Configuration

The driver can be configured through the device tree. Example configuration:

```dts
&i2c0 {
    sb1602: sb1602@3e {
        compatible = "sitronix,sb1602";
        reg = <0x3e>;
    };
};
```

For more details about the implementation, please refer to the source files in this directory.
