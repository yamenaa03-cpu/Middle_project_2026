@echo off

cd "C:\Users\10\Desktop\Middle_project_2026\Assignment2"

"C:\Program Files\Java\jdk-23\bin\java.exe" ^
 --module-path "C:\Users\10\Downloads\openjfx-25.0.1_windows-x64_bin-sdk\javafx-sdk-25.0.1\lib" ^
 --add-modules javafx.controls,javafx.fxml ^
 -jar G22_Prototype_Server.jar

pause
