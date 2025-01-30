# LCD Print Service Project

A BLE-enabled LCD display control system built with nRF Connect SDK, featuring an Android companion app.

## Overview

This project implements a custom BLE service that allows remote control of a 16x2 LCD display (SB1602). It consists of two main parts:

- **Embedded Application**: Built with nRF Connect SDK for nRF5340
- **Android Application**: Modern Android app using Jetpack Compose

## Features

- BLE LCD Print Service (LPS) for remote display control
- 16x2 character LCD support via I2C
- Custom BLE characteristics for printing and clearing display
- Android companion app with Material3 design

## BLE Services

### LCD Print Service (LPS)

- Service UUID: `a00c1710-74ff-4bd5-9e86-cf601d80c054`

#### Characteristics

- Print Characteristic
  - UUID: `a00c1711-74ff-4bd5-9e86-cf601d80c054`
  - Properties: Write
  - Description: Write text to display on LCD

- Clear Characteristic
  - UUID: `a00c1712-74ff-4bd5-9e86-cf601d80c054`
  - Properties: Write
  - Description: Clear LCD screen

## Project Structure

```
├── src/                    # Embedded application source code
│   ├── app/               # Main application logic
│   ├── ble/              # BLE service implementations
│   └── drivers/          # Hardware drivers (LCD, LED, Button)
├── central/              
│   └── android/          # Android companion app
└── boards/               # Board configuration files
```

## Building

### Embedded Application

1. Install nRF Connect SDK
2. Build the project:
```sh
west build
```

### Android Application

1. Open the `central/android` directory in Android Studio
2. Build using Gradle:
```sh
./gradlew build
```

## License

Licensed under the Apache License, Version 2.0

## Documentation

- [BLE Services](src/ble/README.md)
- [Device Drivers](src/drivers/README.md)