cmake_minimum_required(VERSION 3.0 FATAL_ERROR)
set(cppzmq_DIR "zmq_build")
set(SCI_DIR "sci_build/install/lib/cmake/SCI")
project(VFL CXX C)

# place binaries and libraries according to GNU standards
set(CMAKE_CXX_STANDARD 17)
add_definitions(-w)
include(GNUInstallDirs)
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/${CMAKE_INSTALL_LIBDIR})
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/${CMAKE_INSTALL_LIBDIR})
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/${CMAKE_INSTALL_BINDIR})

find_package(Threads)
find_package(cppzmq REQUIRED)
find_package(SCI CONFIG REQUIRED)


add_executable(party Party.cc utils-common.hpp utils-party.hpp xgboost.hpp)
target_link_libraries(
    party
    PRIVATE cppzmq ${CMAKE_THREAD_LIBS_INIT}
)

