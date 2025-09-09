@echo off
setlocal enabledelayedexpansion

REM VulkanMod Extra Version Switching Script (Windows)
REM This script switches the development environment to a specific Minecraft version

set "SCRIPT_DIR=%~dp0"
set "PROJECT_DIR=%SCRIPT_DIR%.."

set "SUPPORTED_VERSIONS=1.21.1 1.21.2 1.21.3 1.21.4"

REM Colors for output (Windows Command Prompt compatible)
set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "NC=[0m"

REM Function to print colored output
print_status() {
    echo %BLUE%[INFO]%NC% %~1
}

print_success() {
    echo %GREEN%[SUCCESS]%NC% %~1
}

print_warning() {
    echo %YELLOW%[WARNING]%NC% %~1
}

print_error() {
    echo %RED%[ERROR]%NC% %~1
}

REM Function to show usage
show_usage() {
    echo Usage: %~n0 ^<version^>
    echo.
    echo Supported versions:
    for %%v in (%SUPPORTED_VERSIONS%) do (
        echo   - %%v
    )
    echo.
    echo Examples:
    echo   %~n0 1.21.3    REM Switch to Minecraft 1.21.3
    echo   %~n0 1.21.1    REM Switch to Minecraft 1.21.1
    echo.
    echo This script will:
    echo 1. Update gradle.properties with the new Minecraft version
    echo 2. Load the version profile from gradle/versions/
    echo 3. Clean and regenerate sources
    echo 4. Show which override directories are active
    echo.
}

REM Function to check if version is supported
is_version_supported() {
    set "target_version=%~1"
    for %%v in (%SUPPORTED_VERSIONS%) do (
        if "%%v"=="%target_version%" exit /b 0
    )
    exit /b 1
}

REM Function to update gradle.properties
update_gradle_properties() {
    set "version=%~1"
    set "gradle_props=%PROJECT_DIR%\gradle.properties"
    
    call :print_status "Updating gradle.properties with Minecraft version %version%..."
    
    REM Create backup of original file
    copy "%gradle_props%" "%gradle_props%.backup.%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%%time:~6,2%" >nul 2>&1
    
    REM Update minecraft_version
    echo minecraft_version=%version% > "%gradle_props%"
    
    REM Update mod version to include the MC version
    set "mod_version=0.2.0-beta6+%version%"
    echo mod_version=%mod_version% >> "%gradle_props%"
    
    REM Copy the rest of the file
    findstr /V "minecraft_version\|mod_version" "%gradle_props%.backup.*" >> "%gradle_props%" 2>nul
    if errorlevel 1 (
        REM If the backup file doesn't exist, use the original
        findstr /V "minecraft_version\|mod_version" "%gradle_props%" > "%gradle_props%.tmp" && move "%gradle_props%.tmp" "%gradle_props%" >nul 2>&1
    )
    
    call :print_success "Updated gradle.properties"
}

REM Function to check version profile
check_version_profile() {
    set "version=%~1"
    set "profile_file=%PROJECT_DIR%\gradle\versions\%version%.properties"
    
    if not exist "%profile_file%" (
        call :print_warning "Version profile not found: %profile_file%"
        call :print_warning "Using default dependencies for version %version%"
        exit /b 1
    ) else (
        call :print_success "Found version profile: %profile_file%"
        exit /b 0
    )
}

REM Function to check override directory
check_override_directory() {
    set "version=%~1"
    set "version_key=!version:.=_!"
    set "override_dir=%PROJECT_DIR%\src\overrides\v!version_key!"
    
    if exist "%override_dir%\java" (
        set "override_count=0"
        for /f %%i in ('dir "%override_dir%\java\*.java" /b ^| find /c /v ""') do set "override_count=%%i"
        call :print_success "Found !override_count! Java override files in %override_dir%\java\"
    ) else (
        call :print_warning "No Java override directory found: %override_dir%\java\"
    )
    
    if exist "%override_dir%\resources" (
        set "resource_count=0"
        for /f %%i in ('dir "%override_dir%\resources\*" /b ^| find /c /v ""') do set "resource_count=%%i"
        call :print_success "Found !resource_count! resource files in %override_dir%\resources\"
    ) else (
        call :print_warning "No resources override directory found: %override_dir%\resources\"
    )
}

REM Function to clean and regenerate
regenerate_project() {
    call :print_status "Cleaning project and regenerating sources..."
    
    cd /d "%PROJECT_DIR%"
    
    REM Run Gradle clean and generate sources
    gradlew.bat clean genSources
    
    if !errorlevel! equ 0 (
        call :print_success "Project regenerated successfully"
    ) else (
        call :print_error "Failed to regenerate project"
        exit /b 1
    )
}

REM Function to update current version file
update_current_version_file() {
    set "version=%~1"
    set "current_version_file=%PROJECT_DIR%\.current-version"
    
    echo %version% > "%current_version_file%"
    call :print_success "Updated .current-version file: %version%"
}

REM Function to show project information
show_project_info() {
    set "version=%~1"
    
    echo.
    echo ==========================================
    echo VulkanMod Extra - Project Information
    echo ==========================================
    echo Target Version: %version%
    echo Project Directory: %PROJECT_DIR%
    echo Current Branch: git branch --show-current 2^>nul ^|^| echo N/A
    echo Git Status: git status --porcelain 2^>nul ^| find /c /v "" ^|^| echo N/A
    echo ==========================================
    echo.
}

REM Function to show IDE setup instructions
show_ide_instructions() {
    set "version=%~1"
    
    call :print_status "IDE Setup Instructions:"
    echo 1. If using IntelliJ IDEA, run './gradlew idea'
    echo 2. If using VS Code, run './gradlew vscode'
    echo 3. Refresh your IDE project
    echo 4. Run './gradlew runClient' to test
    echo.
}

REM Main script logic
:main
REM Check if version is provided
if "%~1"=="" (
    call :print_error "No version specified"
    call :show_usage
    exit /b 1
)

set "target_version=%~1"

REM Check if version is supported
call :is_version_supported "%target_version%"
if !errorlevel! neq 0 (
    call :print_error "Unsupported version: %target_version%"
    call :show_usage
    exit /b 1
)

call :print_status "Switching to Minecraft version: %target_version%"
echo.

REM Show project information
call :show_project_info "%target_version%"

REM Update gradle.properties
call :update_gradle_properties "%target_version%"

REM Check version profile
call :check_version_profile "%target_version%"

REM Update current version file
call :update_current_version_file "%target_version%"

REM Regenerate project
call :regenerate_project

REM Check override directories
echo.
call :print_status "Checking override directories..."
call :check_override_directory "%target_version%"

REM Show next steps
echo.
call :show_ide_instructions "%target_version%"

call :print_success "Version switch completed successfully!"
echo Now you can:
echo   - Run './gradlew runClient' to test the new version
echo   - Create override files if needed: %PROJECT_DIR%\src\overrides\v%target_version:.=_%\
echo   - Build the project: './gradlew build'

:end
endlocal