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
include tests/CMakeFiles/softmax-beacon.dir/depend.make

# Include the progress variables for this target.
include tests/CMakeFiles/softmax-beacon.dir/progress.make

# Include the compile flags for this target's objects.
include tests/CMakeFiles/softmax-beacon.dir/flags.make

tests/CMakeFiles/softmax-beacon.dir/test_floatml_softmax.cpp.o: tests/CMakeFiles/softmax-beacon.dir/flags.make
tests/CMakeFiles/softmax-beacon.dir/test_floatml_softmax.cpp.o: ../tests/test_floatml_softmax.cpp
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/cinwa/SCI/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building CXX object tests/CMakeFiles/softmax-beacon.dir/test_floatml_softmax.cpp.o"
	cd /home/cinwa/SCI/build/tests && /usr/bin/g++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/softmax-beacon.dir/test_floatml_softmax.cpp.o -c /home/cinwa/SCI/tests/test_floatml_softmax.cpp

tests/CMakeFiles/softmax-beacon.dir/test_floatml_softmax.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/softmax-beacon.dir/test_floatml_softmax.cpp.i"
	cd /home/cinwa/SCI/build/tests && /usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /home/cinwa/SCI/tests/test_floatml_softmax.cpp > CMakeFiles/softmax-beacon.dir/test_floatml_softmax.cpp.i

tests/CMakeFiles/softmax-beacon.dir/test_floatml_softmax.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/softmax-beacon.dir/test_floatml_softmax.cpp.s"
	cd /home/cinwa/SCI/build/tests && /usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /home/cinwa/SCI/tests/test_floatml_softmax.cpp -o CMakeFiles/softmax-beacon.dir/test_floatml_softmax.cpp.s

# Object files for target softmax-beacon
softmax__beacon_OBJECTS = \
"CMakeFiles/softmax-beacon.dir/test_floatml_softmax.cpp.o"

# External object files for target softmax-beacon
softmax__beacon_EXTERNAL_OBJECTS =

bin/softmax-beacon: tests/CMakeFiles/softmax-beacon.dir/test_floatml_softmax.cpp.o
bin/softmax-beacon: tests/CMakeFiles/softmax-beacon.dir/build.make
bin/softmax-beacon: lib/libSCI-Beacon.a
bin/softmax-beacon: lib/libSCI-FloatingPoint.a
bin/softmax-beacon: lib/libSCI-BuildingBlocks.a
bin/softmax-beacon: lib/libSCI-Math.a
bin/softmax-beacon: lib/libSCI-GC.a
bin/softmax-beacon: lib/libSCI-LinearOT.a
bin/softmax-beacon: lib/libSCI-OT.a
bin/softmax-beacon: lib/libSCI-FloatingPoint.a
bin/softmax-beacon: lib/libSCI-BuildingBlocks.a
bin/softmax-beacon: lib/libSCI-Math.a
bin/softmax-beacon: lib/libSCI-GC.a
bin/softmax-beacon: lib/libSCI-LinearOT.a
bin/softmax-beacon: lib/libSCI-OT.a
bin/softmax-beacon: /usr/lib/gcc/x86_64-linux-gnu/9/libgomp.so
bin/softmax-beacon: /usr/lib/x86_64-linux-gnu/libpthread.so
bin/softmax-beacon: /usr/lib/x86_64-linux-gnu/libssl.so
bin/softmax-beacon: /usr/lib/x86_64-linux-gnu/libcrypto.so
bin/softmax-beacon: /usr/lib/x86_64-linux-gnu/libgmp.so
bin/softmax-beacon: tests/CMakeFiles/softmax-beacon.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/home/cinwa/SCI/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Linking CXX executable ../bin/softmax-beacon"
	cd /home/cinwa/SCI/build/tests && $(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/softmax-beacon.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
tests/CMakeFiles/softmax-beacon.dir/build: bin/softmax-beacon

.PHONY : tests/CMakeFiles/softmax-beacon.dir/build

tests/CMakeFiles/softmax-beacon.dir/clean:
	cd /home/cinwa/SCI/build/tests && $(CMAKE_COMMAND) -P CMakeFiles/softmax-beacon.dir/cmake_clean.cmake
.PHONY : tests/CMakeFiles/softmax-beacon.dir/clean

tests/CMakeFiles/softmax-beacon.dir/depend:
	cd /home/cinwa/SCI/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/cinwa/SCI /home/cinwa/SCI/tests /home/cinwa/SCI/build /home/cinwa/SCI/build/tests /home/cinwa/SCI/build/tests/CMakeFiles/softmax-beacon.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : tests/CMakeFiles/softmax-beacon.dir/depend

