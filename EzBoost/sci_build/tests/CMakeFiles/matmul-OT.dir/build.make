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
include tests/CMakeFiles/matmul-OT.dir/depend.make

# Include the progress variables for this target.
include tests/CMakeFiles/matmul-OT.dir/progress.make

# Include the compile flags for this target's objects.
include tests/CMakeFiles/matmul-OT.dir/flags.make

tests/CMakeFiles/matmul-OT.dir/test_ring_matmul.cpp.o: tests/CMakeFiles/matmul-OT.dir/flags.make
tests/CMakeFiles/matmul-OT.dir/test_ring_matmul.cpp.o: ../tests/test_ring_matmul.cpp
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/cinwa/SCI/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building CXX object tests/CMakeFiles/matmul-OT.dir/test_ring_matmul.cpp.o"
	cd /home/cinwa/SCI/build/tests && /usr/bin/g++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/matmul-OT.dir/test_ring_matmul.cpp.o -c /home/cinwa/SCI/tests/test_ring_matmul.cpp

tests/CMakeFiles/matmul-OT.dir/test_ring_matmul.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/matmul-OT.dir/test_ring_matmul.cpp.i"
	cd /home/cinwa/SCI/build/tests && /usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /home/cinwa/SCI/tests/test_ring_matmul.cpp > CMakeFiles/matmul-OT.dir/test_ring_matmul.cpp.i

tests/CMakeFiles/matmul-OT.dir/test_ring_matmul.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/matmul-OT.dir/test_ring_matmul.cpp.s"
	cd /home/cinwa/SCI/build/tests && /usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /home/cinwa/SCI/tests/test_ring_matmul.cpp -o CMakeFiles/matmul-OT.dir/test_ring_matmul.cpp.s

# Object files for target matmul-OT
matmul__OT_OBJECTS = \
"CMakeFiles/matmul-OT.dir/test_ring_matmul.cpp.o"

# External object files for target matmul-OT
matmul__OT_EXTERNAL_OBJECTS =

bin/matmul-OT: tests/CMakeFiles/matmul-OT.dir/test_ring_matmul.cpp.o
bin/matmul-OT: tests/CMakeFiles/matmul-OT.dir/build.make
bin/matmul-OT: lib/libSCI-OT.a
bin/matmul-OT: lib/libSCI-LinearOT.a
bin/matmul-OT: lib/libSCI-GC.a
bin/matmul-OT: lib/libSCI-Math.a
bin/matmul-OT: lib/libSCI-BuildingBlocks.a
bin/matmul-OT: lib/libSCI-FloatingPoint.a
bin/matmul-OT: lib/libSCI-OT.a
bin/matmul-OT: lib/libSCI-LinearOT.a
bin/matmul-OT: lib/libSCI-GC.a
bin/matmul-OT: lib/libSCI-Math.a
bin/matmul-OT: lib/libSCI-BuildingBlocks.a
bin/matmul-OT: lib/libSCI-FloatingPoint.a
bin/matmul-OT: /usr/lib/x86_64-linux-gnu/libssl.so
bin/matmul-OT: /usr/lib/x86_64-linux-gnu/libcrypto.so
bin/matmul-OT: /usr/lib/x86_64-linux-gnu/libgmp.so
bin/matmul-OT: /usr/lib/gcc/x86_64-linux-gnu/9/libgomp.so
bin/matmul-OT: /usr/lib/x86_64-linux-gnu/libpthread.so
bin/matmul-OT: tests/CMakeFiles/matmul-OT.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/home/cinwa/SCI/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Linking CXX executable ../bin/matmul-OT"
	cd /home/cinwa/SCI/build/tests && $(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/matmul-OT.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
tests/CMakeFiles/matmul-OT.dir/build: bin/matmul-OT

.PHONY : tests/CMakeFiles/matmul-OT.dir/build

tests/CMakeFiles/matmul-OT.dir/clean:
	cd /home/cinwa/SCI/build/tests && $(CMAKE_COMMAND) -P CMakeFiles/matmul-OT.dir/cmake_clean.cmake
.PHONY : tests/CMakeFiles/matmul-OT.dir/clean

tests/CMakeFiles/matmul-OT.dir/depend:
	cd /home/cinwa/SCI/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/cinwa/SCI /home/cinwa/SCI/tests /home/cinwa/SCI/build /home/cinwa/SCI/build/tests /home/cinwa/SCI/build/tests/CMakeFiles/matmul-OT.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : tests/CMakeFiles/matmul-OT.dir/depend

