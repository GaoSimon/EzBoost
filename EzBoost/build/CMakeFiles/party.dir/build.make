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
CMAKE_SOURCE_DIR = /home/cinwa/my_4th_VFL

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/cinwa/my_4th_VFL/build

# Include any dependencies generated for this target.
include CMakeFiles/party.dir/depend.make

# Include the progress variables for this target.
include CMakeFiles/party.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/party.dir/flags.make

CMakeFiles/party.dir/Party.cc.o: CMakeFiles/party.dir/flags.make
CMakeFiles/party.dir/Party.cc.o: ../Party.cc
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/cinwa/my_4th_VFL/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building CXX object CMakeFiles/party.dir/Party.cc.o"
	/usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/party.dir/Party.cc.o -c /home/cinwa/my_4th_VFL/Party.cc

CMakeFiles/party.dir/Party.cc.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/party.dir/Party.cc.i"
	/usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /home/cinwa/my_4th_VFL/Party.cc > CMakeFiles/party.dir/Party.cc.i

CMakeFiles/party.dir/Party.cc.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/party.dir/Party.cc.s"
	/usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /home/cinwa/my_4th_VFL/Party.cc -o CMakeFiles/party.dir/Party.cc.s

# Object files for target party
party_OBJECTS = \
"CMakeFiles/party.dir/Party.cc.o"

# External object files for target party
party_EXTERNAL_OBJECTS =

bin/party: CMakeFiles/party.dir/Party.cc.o
bin/party: CMakeFiles/party.dir/build.make
bin/party: /usr/lib/x86_64-linux-gnu/libzmq.so
bin/party: CMakeFiles/party.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/home/cinwa/my_4th_VFL/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Linking CXX executable bin/party"
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/party.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
CMakeFiles/party.dir/build: bin/party

.PHONY : CMakeFiles/party.dir/build

CMakeFiles/party.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/party.dir/cmake_clean.cmake
.PHONY : CMakeFiles/party.dir/clean

CMakeFiles/party.dir/depend:
	cd /home/cinwa/my_4th_VFL/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/cinwa/my_4th_VFL /home/cinwa/my_4th_VFL /home/cinwa/my_4th_VFL/build /home/cinwa/my_4th_VFL/build /home/cinwa/my_4th_VFL/build/CMakeFiles/party.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : CMakeFiles/party.dir/depend
