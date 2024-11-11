cmake_minimum_required(VERSION 3.20.0)
find_package(Zephyr REQUIRED HINTS $ENV{ZEPHYR_BASE})

project(sb1602)

target_sources(app PRIVATE
    sb1602b.c
)
