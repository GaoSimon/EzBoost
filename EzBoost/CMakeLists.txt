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
set_directory_properties(PROPERTIES CLEAN_NO_CUSTOM 1)
SET(CMAKE_CXX_FLAGS "-O2")
SET(CMAKE_C_FLAGS "-O2")

find_package(Threads)
find_package(cppzmq REQUIRED)
find_package(SCI CONFIG REQUIRED)

add_executable(party Party.cc utils_party.hpp utils_common.hpp xg_boost.hpp)
target_link_libraries(
    party
    PRIVATE cppzmq ${CMAKE_THREAD_LIBS_INIT}
)
add_executable(user User.cc utils_user.hpp utils_common.hpp)
target_link_libraries(
    user
    PRIVATE cppzmq ${CMAKE_THREAD_LIBS_INIT}
)
add_executable(S1 Server1.cc utils_server.hpp utils_common.hpp xg_boost.hpp)
target_link_libraries(
	S1
    PRIVATE cppzmq ${CMAKE_THREAD_LIBS_INIT}
	SCI::SCI-BuildingBlocks
	SCI::SCI-utils
	SCI::SCI-LinearOT
	SCI::SCI-NonLinear
    SCI::SCI-FloatingPoint
)

add_executable(S2 Server2.cc utils_server.hpp utils_common.hpp xg_boost.hpp)
target_link_libraries(
	S2
    PRIVATE cppzmq ${CMAKE_THREAD_LIBS_INIT}
	SCI::SCI-BuildingBlocks
	SCI::SCI-utils
	SCI::SCI-LinearOT
	SCI::SCI-NonLinear
    SCI::SCI-FloatingPoint
)