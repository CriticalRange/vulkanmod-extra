#!/bin/bash

# VulkanMod Extra Version Switching Script
# This script switches the development environment to a specific Minecraft version

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
SUPPORTED_VERSIONS=("1.21.1" "1.21.2" "1.21.3" "1.21.4")

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 <version>"
    echo ""
    echo "Supported versions:"
    for version in "${SUPPORTED_VERSIONS[@]}"; do
        echo "  - $version"
    done
    echo ""
    echo "Examples:"
    echo "  $0 1.21.3    # Switch to Minecraft 1.21.3"
    echo "  $0 1.21.1    # Switch to Minecraft 1.21.1"
    echo ""
    echo "This script will:"
    echo "1. Update gradle.properties with the new Minecraft version"
    echo "2. Load the version profile from gradle/versions/"
    echo "3. Clean and regenerate sources"
    echo "4. Show which override directories are active"
    echo ""
}

# Function to check if version is supported
is_version_supported() {
    local version="$1"
    for supported in "${SUPPORTED_VERSIONS[@]}"; do
        if [[ "$version" == "$supported" ]]; then
            return 0
        fi
    done
    return 1
}

# Function to update gradle.properties
update_gradle_properties() {
    local version="$1"
    local gradle_props="$PROJECT_DIR/gradle.properties"
    
    print_status "Updating gradle.properties with Minecraft version $version..."
    
    # Create backup of original file
    cp "$gradle_props" "$gradle_props.backup.$(date +%Y%m%d_%H%M%S)"
    
    # Update minecraft_version
    sed -i.tmp "s/^minecraft_version=.*/minecraft_version=$version/" "$gradle_props"
    
    # Update mod version to include the MC version
    local mod_version="0.2.0-beta6+$version"
    sed -i.tmp "s/^mod_version=.*/mod_version=$mod_version/" "$gradle_props"
    
    # Clean up temporary files
    rm -f "$gradle_props.tmp"
    
    print_success "Updated gradle.properties"
}

# Function to check if version profile exists
check_version_profile() {
    local version="$1"
    local profile_file="$PROJECT_DIR/gradle/versions/$version.properties"
    
    if [[ ! -f "$profile_file" ]]; then
        print_warning "Version profile not found: $profile_file"
        print_warning "Using default dependencies for version $version"
        return 1
    else
        print_success "Found version profile: $profile_file"
        return 0
    fi
}

# Function to check override directory
check_override_directory() {
    local version="$1"
    local version_key="${version//./_}"
    local override_dir="$PROJECT_DIR/src/overrides/v$version_key"
    
    if [[ -d "$override_dir/java" ]]; then
        local override_count=$(find "$override_dir/java" -name "*.java" | wc -l)
        print_success "Found $override_count Java override files in $override_dir/java/"
    else
        print_warning "No Java override directory found: $override_dir/java/"
    fi
    
    if [[ -d "$override_dir/resources" ]]; then
        local resource_count=$(find "$override_dir/resources" -type f | wc -l)
        print_success "Found $resource_count resource files in $override_dir/resources/"
    else
        print_warning "No resources override directory found: $override_dir/resources/"
    fi
}

# Function to clean and regenerate
regenerate_project() {
    print_status "Cleaning project and regenerating sources..."
    
    cd "$PROJECT_DIR"
    
    # Run Gradle clean and generate sources
    ./gradlew clean genSources
    
    if [[ $? -eq 0 ]]; then
        print_success "Project regenerated successfully"
    else
        print_error "Failed to regenerate project"
        exit 1
    fi
}

# Function to update current version file
update_current_version_file() {
    local version="$1"
    local current_version_file="$PROJECT_DIR/.current-version"
    
    echo "$version" > "$current_version_file"
    print_success "Updated .current-version file: $version"
}

# Function to show project information
show_project_info() {
    local version="$1"
    
    echo ""
    echo "=========================================="
    echo "VulkanMod Extra - Project Information"
    echo "=========================================="
    echo "Target Version: $version"
    echo "Project Directory: $PROJECT_DIR"
    echo "Current Branch: $(cd "$PROJECT_DIR" && git branch --show-current 2>/dev/null || echo 'N/A')"
    echo "Git Status: $(cd "$PROJECT_DIR" && git status --porcelain 2>/dev/null | wc -l | xargs echo 'files modified' || echo 'N/A')"
    echo "=========================================="
    echo ""
}

# Function to show IDE setup instructions
show_ide_instructions() {
    local version="$1"
    
    print_status "IDE Setup Instructions:"
    echo "1. If using IntelliJ IDEA, run './gradlew idea'"
    echo "2. If using VS Code, run './gradlew vscode'"
    echo "3. Refresh your IDE project"
    echo "4. Run './gradlew runClient' to test"
    echo ""
}

# Main script logic
main() {
    # Check if version is provided
    if [[ $# -eq 0 ]]; then
        print_error "No version specified"
        show_usage
        exit 1
    fi
    
    local target_version="$1"
    
    # Check if version is supported
    if ! is_version_supported "$target_version"; then
        print_error "Unsupported version: $target_version"
        show_usage
        exit 1
    fi
    
    print_status "Switching to Minecraft version: $target_version"
    echo ""
    
    # Show project information
    show_project_info "$target_version"
    
    # Update gradle.properties
    update_gradle_properties "$target_version"
    
    # Check version profile
    check_version_profile "$target_version"
    
    # Update current version file
    update_current_version_file "$target_version"
    
    # Regenerate project
    regenerate_project
    
    # Check override directories
    echo ""
    print_status "Checking override directories..."
    check_override_directory "$target_version"
    
    # Show next steps
    echo ""
    show_ide_instructions "$target_version"
    
    print_success "Version switch completed successfully!"
    echo "Now you can:"
    echo "  - Run './gradlew runClient' to test the new version"
    echo "  - Create override files if needed: $PROJECT_DIR/src/overrides/v${target_version//./_}/"
    echo "  - Build the project: './gradlew build'"
}

# Run main function with all arguments
main "$@"