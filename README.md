# HoldOn
## Description
***HoldOn*** is a **Windows x64** software designed to intercept and edit network packets before their sending/delivery from/to a process.
## Requirements
- You must have installed the [Visual Studio 2019 packages](https://visualstudio.microsoft.com/fr/downloads/)  
- You must have installed the [MinGW msys compiler packages](https://sourceforge.net/projects/mingw/)  
- You must have installed the [Java SE Development Kit 8](https://www.oracle.com/fr/java/technologies/javase/javase-jdk8-downloads.html)  
- You must have configured your **environment variables** the following way:  
  - **JAVA_HOME**: Must contain `*\Java\jdk8XXXX-XXX\`  
  - **Path**: Must contain `*\MinGW\msys\1.0\bin\` and `%JAVA_HOME%\bin\`
## Configuration
- **prepare_env.bat** :
  - You must set **WIN_KITS_VERSION** to the name of the directory located under `*\Windows Kits\XX\Include\` and that contains a directory named `ucrt`.  
- **Makefile** :
  - You must set **MSVC_VERSION** to the name of the directory located under `*\Microsoft Visual Studio\2019\Community\VC\Tools\MSVC\`  
  - You must set **WIN_KITS_VERSION** to the name of the directory located under `*\Windows Kits\XX\Include\` and that contains a directory named `ucrt`.
## Compilation
1. Open a terminal in ***HoldOn*** root directory.
2. Enter command `prepare_env` to **setup your environment variables** for the Makefile.
3. Enter command `make clean` to **remove files from previous builds**.
4. Enter command `make` or `make HoldOn` to run the Makefile that will **compile all sources and produce the executable**.
## Execution
1. From ***HoldOn*** root directory, get into `.\build`
2. Start `run.bat`
## How to use
1. The first thing to do after ***HoldOn*** is started, is to **attach to the process of your choice**.
2. If the process sends or receives network packets, **you should see them in the console (left panel) and the graph (top right panel)**.
3. To start editing them, you need to **press the pause button** (<img src="https://github.com/HellDiner/HoldOn/blob/main/app/res/img/pause.png" width="13" alt="Pause button image" title="Pause button">) and **wait for the next packet to load**.
4. Then, you can edit the packet by **changing values in both the HEX and ASCII panels**.
5. Finally, you need to resume the process **by pressing the resume button** (<img src="https://github.com/HellDiner/HoldOn/blob/main/app/res/img/play.png" width="13" alt="Resume button image" title="Resume button">) or wait for the next packet to load **by pressing the single-step button** (<img src="https://github.com/HellDiner/HoldOn/blob/main/app/res/img/single_step.png" width="13" alt="Single-step button image" title="Single-step button">).
## Support
### Discord support server
**If you need any help with the software, please join the [Discord support server](https://discord.gg/bNNWBnk).**

### Support me ;)
**This project was not designed for profit but any donation is welcome :**  
  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/donate?hosted_button_id=FGPVL34PVQVZJ)
