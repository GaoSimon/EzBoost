# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.18

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


# Disable VCS-based implicit rules.
% : %,v


# Disable VCS-based implicit rules.
% : RCS/%


# Disable VCS-based implicit rules.
% : RCS/%,v


# Disable VCS-based implicit rules.
% : SCCS/s.%


# Disable VCS-based implicit rules.
% : s.%


.SUFFIXES: .hpux_make_needs_suffix_list


# Command-line flag to silence nested $(MAKE).
$(VERBOSE)MAKESILENT = -s

#Suppress display of executed commands.
$(VERBOSE).SILENT:

# A target that is always out of date.
cmake_force:

.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /opt/cmake-3.18.0/bin/cmake

# The command to remove a file.
RM = /opt/cmake-3.18.0/bin/cmake -E rm -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/cinwa/cppzmq-4.9.0

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/cinwa/cppzmq-4.9.0/build

# Utility rule file for ContinuousConfigure.

# Include the progress variables for this target.
include tests/CMakeFiles/ContinuousConfigure.dir/progress.make

tests/CMakeFiles/ContinuousConfigure:
	cd /home/cinwa/cppzmq-4.9.0/build/tests && /opt/cmake-3.18.0/bin/ctest -D ContinuousConfigure

ContinuousConfigure: tests/CMakeFiles/ContinuousConfigure
ContinuousConfigure: tests/CMakeFiles/ContinuousConfigure.dir/build.make

.PHONY : ContinuousConfigure

# Rule to build all files generated by this target.
tests/CMakeFiles/ContinuousConfigure.dir/build: ContinuousConfigure

.PHONY : tests/CMakeFiles/ContinuousConfigure.dir/build

tests/CMakeFiles/ContinuousConfigure.dir/clean:
	cd /home/cinwa/cppzmq-4.9.0/build/tests && $(CMAKE_COMMAND) -P CMakeFiles/ContinuousConfigure.dir/cmake_clean.cmake
.PHONY : tests/CMakeFiles/ContinuousConfigure.dir/clean

tests/CMakeFiles/ContinuousConfigure.dir/depend:
	cd /home/cinwa/cppzmq-4.9.0/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/cinwa/cppzmq-4.9.0 /home/cinwa/cppzmq-4.9.0/tests /home/cinwa/cppzmq-4.9.0/build /home/cinwa/cppzmq-4.9.0/build/tests /home/cinwa/cppzmq-4.9.0/build/tests/CMakeFiles/ContinuousConfigure.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : tests/CMakeFiles/ContinuousConfigure.dir/depend

