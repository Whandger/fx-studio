@echo off
setlocal
cd /d "%~dp0"
title Minecraft FX Studio
echo Iniciando Minecraft FX Studio...

if exist "gradlew.bat" goto :use_wrapper

where gradle >nul 2>nul
if %errorlevel% equ 0 goto :use_global

echo [ERRO] Gradle nao encontrado e gradlew.bat ausente nesta pasta.
pause
exit /b 1

:use_wrapper
call gradlew.bat run
goto :check_error

:use_global
call gradle run
goto :check_error

:check_error
if %errorlevel% neq 0 (
    echo.
    echo [ERRO] O programa fechou com codigo de erro %errorlevel%.
    pause
)
endlocal