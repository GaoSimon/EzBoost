# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.16

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list


# Suppress display of executed commands.
$(VERBOSE).SILENT:


# A target that is always out of date.
cmake_force:

.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/cinwa/SCI

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/cinwa/SCI/build

# Include any dependencies generated for this target.
include tests/GC/CMakeFiles/bit.dir/depend.make

# Include the progress variables for this target.
include tests/GC/CMakeFiles/bit.dir/progress.make

# Include the compile flags for this target's objects.
include tests/GC/CMakeFiles/bit.dir/flags.make

tests/GC/CMakeFiles/bit.dir/test_bit.cpp.o: tests/GC/CMakeFiles/bit.dir/flags.make
tests/GC/CMakeFiles/bit.dir/test_bit.cpp.o: ../tests/GC/test_bit.cpp
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/cinwa/SCI/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building CXX object tests/GC/CMakeFiles/bit.dir/test_bit.cpp.o"
	cd /home/cinwa/SCI/build/tests/GC && /usr/bin/g++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/bit.dir/test_bit.cpp.o -c /home/cinwa/SCI/tests/GC/test_bit.cpp

tests/GC/CMakeFiles/bit.dir/test_bit.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/bit.dir/test_bit.cpp.i"
	cd /home/cinwa/SCI/build/tests/GC && /usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /home/cinwa/SCI/tests/GC/test_bit.cpp > CMakeFiles/bit.dir/test_bit.cpp.i

tests/GC/CMakeFiles/bit.dir/test_bit.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/bit.dir/test_bit.cpp.s"
	cd /home/cinwa/SCI/build/tests/GC && /usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /home/cinwa/SCI/tests/GC/test_bit.cpp -o CMakeFiles/bit.dir/test_bit.cpp.s

# Object files for target bit
bit_OBJECTS = \
"CMakeFiles/bit.dir/test_bit.cpp.o"

# External object files for target bit
bit_EXTERNAL_OBJECTS =

bin/bit: tests/GC/CMakeFiles/bit.dir/test_bit.cpp.o
bin/bit: tests/GC/CMakeFiles/bit.dir/build.make
bin/bit: lib/libSCI-OT.a
bin/bit: lib/libSCI-LinearOT.a
bin/bit: lib/libSCI-GC.a
bin/bit: lib/libSCI-Math.a
bin/bit: lib/libSCI-BuildingBlocks.a
bin/bit: lib/libSCI-FloatingPoint.a
bin/bit: lib/libSCI-OT.a
bin/bit: lib/libSCI-LinearOT.a
bin/bit: lib/libSCI-GC.a
bin/bit: lib/libSCI-Math.a
bin/bit: lib/libSCI-BuildingBlocks.a
bin/bit: lib/libSCI-FloatingPoint.a
bin/bit: /usr/lib/x86_64-linux-gnu/libssl.so
bin/bit: /usr/lib/x86_64-linux-gnu/libcrypto.so
bin/bit: /usr/lib/x86_64-linux-gnu/libgmp.so
bin/bit: /usr/lib/gcc/x86_64-linux-gnu/9/libgomp.so
bin/bit: /usr/lib/x86_64-linux-gnu/libpthread.so
bin/bit: tests/GC/CMakeFiles/bit.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/home/cinwa/SCI/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Linking CXX executable ../../bin/bit"
	cd /home/cinwa/SCI/build/tests/GC && $(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/bit.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
tests/GC/CMakeFiles/bit.dir/build: bin/bit

.PHONY : tests/GC/CMakeFiles/bit.dir/build

tests/GC/CMakeFiles/bit.dir/clean:
	cd /home/cinwa/SCI/build/tests/GC && $(CMAKE_COMMAND) -P CMakeFiles/bit.dir/cmake_clean.cmake
.PHONY : tests/GC/CMakeFiles/bit.dir/clean

tests/GC/CMakeFiles/bit.dir/depend:
	cd /home/cinwa/SCI/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/cinwa/SCI /home/cinwa/SCI/tests/GC /home/cinwa/SCI/build /home/cinwa/SCI/build/tests/GC /home/cinwa/SCI/build/tests/GC/CMakeFiles/bit.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : tests/GC/CMakeFiles/bit.dir/depend

