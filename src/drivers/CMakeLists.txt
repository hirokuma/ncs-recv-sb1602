cmake_minimum_required(VERSION 3.20.0)
find_package(Zephyr REQUIRED HINTS $ENV{ZEPHYR_BASE})

project(drivers)

add_subdirectory(led)
add_subdirectory(sb1602)
