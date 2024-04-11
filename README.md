# Kalkulator

Calculator desktop app made in JavaFx, that I created for my User Interfaces class at uni. General look and feel resembles the default calculator app for Windows 10.

![Calculator app screenshot](https://raw.githubusercontent.com/markokroselj/calculator/main/screen_capture.png)


## Features

- supports basic operations ``+,-,*,/``,
- correct order of calculations,
- constants ``pi``, ``e``,
- factorial,
- calculation history,
    - open and save history to a file
- clear just display,
- clear display and history,
- event log.


## Key Bindings

Some feautures have a dedicated key binding.

| Binding  | Description                                                |
|----------|------------------------------------------------------------|
| <kbd>Ctrl + O</kbd>| Open a file chooser dialog to open a calcolation history file                            |
| <kbd>Ctrl + S</kbd> | Open a file chooser dialog to save the calculation history to a file
| <kbd>Ctrl + C</kbd> | Clear calculator display content and calculation history   |
| <kbd>Ctrl + Q</kbd> | Exit the program                                           |
| <kbd>F1</kbd> | Open the pdf with help                                          |
   
## Built from source

The easiest way to get started developing this app is to download (Intellij IDEA)[https://www.jetbrains.com/idea/] IDE and Java jdk 11. Clone and open this project into Intelij and IDE should automatically download all the necessary libraries. To build the jar file click Build -> Build artifacts. Jar file will be located under ``out/artifacts/UV_DN2_jar/UV_DN2.jar``

Alternatively you can download and setup Gradle on your system and run:

``gradle ./run``


