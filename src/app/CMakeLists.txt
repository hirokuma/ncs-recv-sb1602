cmake_minimum_required(VERSION 3.20.0)
find_package(Zephyr REQUIRED HINTS $ENV{ZEPHYR_BASE})

project(src)

target_sources(app PRIVATE
    ble_lps.c
    ble_lbs.c
    app.c
)
target_include_directories(app PRIVATE
    ..
)
