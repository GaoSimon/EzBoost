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
include src/CMakeFiles/SCI-OT.dir/depend.make

# Include the progress variables for this target.
include src/CMakeFiles/SCI-OT.dir/progress.make

# Include the compile flags for this target's objects.
include src/CMakeFiles/SCI-OT.dir/flags.make

src/CMakeFiles/SCI-OT.dir/library_fixed_uniform.cpp.o: src/CMakeFiles/SCI-OT.dir/flags.make
src/CMakeFiles/SCI-OT.dir/library_fixed_uniform.cpp.o: ../src/library_fixed_uniform.cpp
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/cinwa/SCI/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building CXX object src/CMakeFiles/SCI-OT.dir/library_fixed_uniform.cpp.o"
	cd /home/cinwa/SCI/build/src && /usr/bin/g++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/SCI-OT.dir/library_fixed_uniform.cpp.o -c /home/cinwa/SCI/src/library_fixed_uniform.cpp

src/CMakeFiles/SCI-OT.dir/library_fixed_uniform.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/SCI-OT.dir/library_fixed_uniform.cpp.i"
	cd /home/cinwa/SCI/build/src && /usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /home/cinwa/SCI/src/library_fixed_uniform.cpp > CMakeFiles/SCI-OT.dir/library_fixed_uniform.cpp.i

src/CMakeFiles/SCI-OT.dir/library_fixed_uniform.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/SCI-OT.dir/library_fixed_uniform.cpp.s"
	cd /home/cinwa/SCI/build/src && /usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /home/cinwa/SCI/src/library_fixed_uniform.cpp -o CMakeFiles/SCI-OT.dir/library_fixed_uniform.cpp.s

src/CMakeFiles/SCI-OT.dir/library_fixed.cpp.o: src/CMakeFiles/SCI-OT.dir/flags.make
src/CMakeFiles/SCI-OT.dir/library_fixed.cpp.o: ../src/library_fixed.cpp
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/cinwa/SCI/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Building CXX object src/CMakeFiles/SCI-OT.dir/library_fixed.cpp.o"
	cd /home/cinwa/SCI/build/src && /usr/bin/g++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/SCI-OT.dir/library_fixed.cpp.o -c /home/cinwa/SCI/src/library_fixed.cpp

src/CMakeFiles/SCI-OT.dir/library_fixed.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/SCI-OT.dir/library_fixed.cpp.i"
	cd /home/cinwa/SCI/build/src && /usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /home/cinwa/SCI/src/library_fixed.cpp > CMakeFiles/SCI-OT.dir/library_fixed.cpp.i

src/CMakeFiles/SCI-OT.dir/library_fixed.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/SCI-OT.dir/library_fixed.cpp.s"
	cd /home/cinwa/SCI/build/src && /usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /home/cinwa/SCI/src/library_fixed.cpp -o CMakeFiles/SCI-OT.dir/library_fixed.cpp.s

src/CMakeFiles/SCI-OT.dir/globals.cpp.o: src/CMakeFiles/SCI-OT.dir/flags.make
src/CMakeFiles/SCI-OT.dir/globals.cpp.o: ../src/globals.cpp
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/cinwa/SCI/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_3) "Building CXX object src/CMakeFiles/SCI-OT.dir/globals.cpp.o"
	cd /home/cinwa/SCI/build/src && /usr/bin/g++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/SCI-OT.dir/globals.cpp.o -c /home/cinwa/SCI/src/globals.cpp

src/CMakeFiles/SCI-OT.dir/globals.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/SCI-OT.dir/globals.cpp.i"
	cd /home/cinwa/SCI/build/src && /usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /home/cinwa/SCI/src/globals.cpp > CMakeFiles/SCI-OT.dir/globals.cpp.i

src/CMakeFiles/SCI-OT.dir/globals.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/SCI-OT.dir/globals.cpp.s"
	cd /home/cinwa/SCI/build/src && /usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /home/cinwa/SCI/src/globals.cpp -o CMakeFiles/SCI-OT.dir/globals.cpp.s

src/CMakeFiles/SCI-OT.dir/cleartext_library_fixed.cpp.o: src/CMakeFiles/SCI-OT.dir/flags.make
src/CMakeFiles/SCI-OT.dir/cleartext_library_fixed.cpp.o: ../src/cleartext_library_fixed.cpp
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/cinwa/SCI/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_4) "Building CXX object src/CMakeFiles/SCI-OT.dir/cleartext_library_fixed.cpp.o"
	cd /home/cinwa/SCI/build/src && /usr/bin/g++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/SCI-OT.dir/cleartext_library_fixed.cpp.o -c /home/cinwa/SCI/src/cleartext_library_fixed.cpp

src/CMakeFiles/SCI-OT.dir/cleartext_library_fixed.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/SCI-OT.dir/cleartext_library_fixed.cpp.i"
	cd /home/cinwa/SCI/build/src && /usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /home/cinwa/SCI/src/cleartext_library_fixed.cpp > CMakeFiles/SCI-OT.dir/cleartext_library_fixed.cpp.i

src/CMakeFiles/SCI-OT.dir/cleartext_library_fixed.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/SCI-OT.dir/cleartext_library_fixed.cpp.s"
	cd /home/cinwa/SCI/build/src && /usr/bin/g++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /home/cinwa/SCI/src/cleartext_library_fixed.cpp -o CMakeFiles/SCI-OT.dir/cleartext_library_fixed.cpp.s

# Object files for target SCI-OT
SCI__OT_OBJECTS = \
"CMakeFiles/SCI-OT.dir/library_fixed_uniform.cpp.o" \
"CMakeFiles/SCI-OT.dir/library_fixed.cpp.o" \
"CMakeFiles/SCI-OT.dir/globals.cpp.o" \
"CMakeFiles/SCI-OT.dir/cleartext_library_fixed.cpp.o"

# External object files for target SCI-OT
SCI__OT_EXTERNAL_OBJECTS =

lib/libSCI-OT.a: src/CMakeFiles/SCI-OT.dir/library_fixed_uniform.cpp.o
lib/libSCI-OT.a: src/CMakeFiles/SCI-OT.dir/library_fixed.cpp.o
lib/libSCI-OT.a: src/CMakeFiles/SCI-OT.dir/globals.cpp.o
lib/libSCI-OT.a: src/CMakeFiles/SCI-OT.dir/cleartext_library_fixed.cpp.o
lib/libSCI-OT.a: src/CMakeFiles/SCI-OT.dir/build.make
lib/libSCI-OT.a: src/CMakeFiles/SCI-OT.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/home/cinwa/SCI/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_5) "Linking CXX static library ../lib/libSCI-OT.a"
	cd /home/cinwa/SCI/build/src && $(CMAKE_COMMAND) -P CMakeFiles/SCI-OT.dir/cmake_clean_target.cmake
	cd /home/cinwa/SCI/build/src && $(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/SCI-OT.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
src/CMakeFiles/SCI-OT.dir/build: lib/libSCI-OT.a

.PHONY : src/CMakeFiles/SCI-OT.dir/build

src/CMakeFiles/SCI-OT.dir/clean:
	cd /home/cinwa/SCI/build/src && $(CMAKE_COMMAND) -P CMakeFiles/SCI-OT.dir/cmake_clean.cmake
.PHONY : src/CMakeFiles/SCI-OT.dir/clean

src/CMakeFiles/SCI-OT.dir/depend:
	cd /home/cinwa/SCI/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/cinwa/SCI /home/cinwa/SCI/src /home/cinwa/SCI/build /home/cinwa/SCI/build/src /home/cinwa/SCI/build/src/CMakeFiles/SCI-OT.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : src/CMakeFiles/SCI-OT.dir/depend

