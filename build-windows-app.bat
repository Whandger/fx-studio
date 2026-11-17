@echo off
setlocal
cd /d "%~dp0"
title Build Minecraft FX Studio (jpackage)
echo Gerando pacote executavel para Windows...

if exist "gradlew.bat" (
    call gradlew.bat clean fatJar
) else (
    call gradle clean fatJar
)

if %errorlevel% neq 0 (
    echo [ERRO] Falha ao gerar o JAR.
    pause
    exit /b 1
)

if exist "dist" rmdir /s /q "dist"
jpackage ^
  --input build/libs ^
  --name "MinecraftFXStudio" ^
  --main-jar FX-Studio-1.0.0-all.jar ^
  --main-class com.fxstudio.FxStudioLauncher ^
  --type app-image ^
  --dest dist ^
  --java-options "-Xmx1024m"

if %errorlevel% equ 0 (
    echo [SUCESSO] Aplicativo gerado em dist\MinecraftFXStudio\MinecraftFXStudio.exe
) else (
    echo [AVISO] Certifique-se de que o JDK 21 (com jpackage) esta no PATH.
)
pause
endlocal