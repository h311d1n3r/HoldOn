CC="C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC\Tools\MSVC\14.28.29333\bin\Hostx64\x64\cl"

UNIV_CRT_INC="C:\Program Files (x86)\Windows Kits\10\Include\10.0.18362.0\ucrt"
UNIV_CRT_LIB="C:\Program Files (x86)\Windows Kits\10\Lib\10.0.18362.0\ucrt\x64"
JNI_INC="$(JAVA_HOME)\include"
JNI_MD_INC="$(JAVA_HOME)\include\win32"

SHARED_HEADERS_INC="./engine/shared_headers"
LETSHOOK_INC="./LetsHook/include"
LETSHOOK_LIB="./LetsHook/build"

COMPILE_FLAGS=-D_USRDLL -D_WINDLL -EHsc -std:c++17

HoldOn:	LetsHook.dll injector.dll net_hooks.dll pipe_server.dll HoldOn.jar
		mkdir -p build/lib/hook
		cp ./LetsHook/build/LetsHook.dll ./build/lib/hook
		cp ./engine/injector/build/injector.dll ./build/lib
		cp ./engine/net_hooks/build/net_hooks.dll ./build/lib/hook
		cp ./engine/pipe_server/build/pipe_server.dll ./build/lib
		cp ./app/build/HoldOn.jar ./build
		touch ./build/run.bat
		echo "java -Xms1G -jar ./HoldOn.jar" > ./build/run.bat

LetsHook.dll:
			  mkdir -p ./LetsHook/build
			  mkdir -p ./LetsHook/obj
			  $(CC) $(COMPILE_FLAGS) ./LetsHook/src/*.cpp -Fo"./LetsHook/obj/" -I"./LetsHook/include" -I"$(INCLUDE)" -I$(UNIV_CRT_INC) -link -LIBPATH:"$(LIBPATH)" -LIBPATH:$(UNIV_CRT_LIB) -dll -out:./LetsHook/build/$@

injector.dll:
			  mkdir -p ./engine/injector/build
			  mkdir -p ./engine/injector/obj
			  $(CC) $(COMPILE_FLAGS) ./engine/injector/src/*.cpp -Fo"./engine/injector/obj/" -I"./engine/injector/include" -I"$(INCLUDE)" -I$(UNIV_CRT_INC) -I$(JNI_INC) -I$(JNI_MD_INC) -link -LIBPATH:"$(LIBPATH)" -LIBPATH:$(UNIV_CRT_LIB) -dll -out:./engine/injector/build/$@
			  
net_hooks.dll: LetsHook.dll
			   mkdir -p ./engine/net_hooks/build
			   mkdir -p ./engine/net_hooks/obj
			   $(CC) $(COMPILE_FLAGS) ./engine/net_hooks/src/*.cpp -Fo"./engine/net_hooks/obj/" -I"./engine/net_hooks/include" -I"$(INCLUDE)" -I$(UNIV_CRT_INC) -I$(SHARED_HEADERS_INC) -I$(LETSHOOK_INC) -link -LIBPATH:"$(LIBPATH)" -LIBPATH:$(UNIV_CRT_LIB) -LIBPATH:$(LETSHOOK_LIB) -dll -out:./engine/net_hooks/build/$@

pipe_server.dll:
			     mkdir -p ./engine/pipe_server/build
			     mkdir -p ./engine/pipe_server/obj
			     $(CC) $(COMPILE_FLAGS) ./engine/pipe_server/src/*.cpp -Fo"./engine/pipe_server/obj/" -I"./engine/pipe_server/include" -I"$(INCLUDE)" -I$(UNIV_CRT_INC) -I$(SHARED_HEADERS_INC) -I$(JNI_INC) -I$(JNI_MD_INC) -link -LIBPATH:"$(LIBPATH)" -LIBPATH:$(UNIV_CRT_LIB) -dll -out:./engine/pipe_server/build/$@

HoldOn.jar:
			mkdir ./app/class
			mkdir ./app/build
			javac -d ./app/class -sourcepath ./app/src ./app/src/me/helldiner/holdon/main/Main.java
			jar cfe ./app/build/HoldOn.jar me.helldiner.holdon.main.Main -C ./app/class . -C ./app res
			  
clean:
	rm -rf ./LetsHook/build/
	rm -rf ./LetsHook/obj/
	rm -rf ./engine/injector/build
	rm -rf ./engine/injector/obj
	rm -rf ./engine/net_hooks/build
	rm -rf ./engine/net_hooks/obj
	rm -rf ./engine/pipe_server/build
	rm -rf ./engine/pipe_server/obj
	rm -rf ./app/class
	rm -rf ./app/build
	rm -rf ./build
