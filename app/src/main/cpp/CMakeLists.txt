cmake_minimum_required(VERSION 3.22.1)
project(mcjava_launcher LANGUAGES CXX)

add_library(launcher SHARED native-lib.cpp)
find_library(android-log log)
target_link_libraries(launcher ${android-log})
