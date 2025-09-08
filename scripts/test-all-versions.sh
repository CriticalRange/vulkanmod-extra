#!/bin/bash

# VulkanMod Extra - Test All Versions Script
# Tests all supported Minecraft versions for compatibility issues

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
SUPPORTED_VERSIONS=("1.21.1" "1.21.2" "1.21.3" "1.21.4")
RESULTS_DIR="$PROJECT_DIR/test-results"
SUMMARY_FILE="$RESULTS_DIR/compatibility-report.md"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Create results directory
mkdir -p "$RESULTS_DIR"

# Function to test a specific version
test_version() {
    local version="$1"
    local version_key="${version//./_}"
    local version_dir="$RESULTS_DIR/version-$version_key"
    local build_log="$version_dir/build.log"
    local test_log="$version_dir/test.log"
    
    print_status "Testing Minecraft version: $version"
    mkdir -p "$version_dir"
    
    # Switch to version
    print_status "Switching to version $version..."
    bash "$SCRIPT_DIR/switch-version.sh" "$version" > "$version_dir/switch.log" 2>&1
    
    if [ $? -ne 0 ]; then
        print_error "Failed to switch to version $version"
        echo "FAILED" > "$version_dir/status"
        return 1
    fi
    
    # Build the project
    print_status "Building for version $version..."
    cd "$PROJECT_DIR"
    timeout 600 ./gradlew build --no-daemon > "$build_log" 2>&1
    
    BUILD_RESULT=$?
    if [ $BUILD_RESULT -eq 0 ]; then
        print_success "Build successful for version $version"
        echo "SUCCESS" > "$version_dir/status"
        
        # Test compilation
        print_status "Testing compilation..."
        ./gradlew compileJava --no-daemon >> "$build_log" 2>&1
        
        if [ $? -eq 0 ]; then
            print_success "Compilation test passed for version $version"
            echo "PASSED" > "$version_dir/compilation"
        else
            print_warning "Compilation test failed for version $version"
            echo "FAILED" > "$version_dir/compilation"
        fi
    else
        print_error "Build failed for version $version"
        echo "FAILED" > "$version_dir/status"
        echo "Build failed" > "$version_dir/compilation"
    fi
    
    # Test mixin loading
    print_status "Testing mixin configuration..."
    ./gradlew remapJar --no-daemon >> "$build_log" 2>&1
    
    if [ $? -eq 0 ]; then
        echo "PASSED" > "$version_dir/mixins"
    else
        print_warning "Mixin test failed for version $version"
        echo "FAILED" > "$version_dir/mixins"
    fi
    
    # Check override directory
    if [ -d "src/overrides/v$version_key" ]; then
        local override_count=$(find "src/overrides/v$version_key/java" -name "*.java" | wc -l)
        echo "$override_count" > "$version_dir/override-count"
        print_status "Found $override_count override files for version $version"
    else
        echo "0" > "$version_dir/override-count"
        print_status "No override directory for version $version"
    fi
    
    # Store version info
    echo "$version" > "$version_dir/version"
    echo "$(cat gradle.properties | grep minecraft_version)" > "$version_dir/gradle-info"
    
    cd "$SCRIPT_DIR"
}

# Function to generate compatibility report
generate_report() {
    print_status "Generating compatibility report..."
    
    {
        echo "# VulkanMod Extra - Multi-Version Compatibility Report"
        echo ""
        echo "Generated on: $(date)"
        echo ""
        echo "| Version | Status | Compilation | Mixins | Overrides | Issues |"
        echo "|---------|--------|-------------|--------|-----------|--------|"
        
        TOTAL_VERSIONS=0
        SUCCESSFUL_VERSIONS=0
        FAILED_VERSIONS=0
        
        for version in "${SUPPORTED_VERSIONS[@]}"; do
            local version_key="${version//./_}"
            local version_dir="$RESULTS_DIR/version-$version_key"
            
            if [ -f "$version_dir/status" ]; then
                TOTAL_VERSIONS=$((TOTAL_VERSIONS + 1))
                local status=$(cat "$version_dir/status")
                local compilation=$(cat "$version_dir/compilation" 2>/dev/null || echo "UNKNOWN")
                local mixins=$(cat "$version_dir/mixins" 2>/dev/null || echo "UNKNOWN")
                local overrides=$(cat "$version_dir/override-count" 2>/dev/null || echo "0")
                
                if [ "$status" = "SUCCESS" ]; then
                    SUCCESSFUL_VERSIONS=$((SUCCESSFUL_VERSIONS + 1))
                    local emoji="✅"
                else
                    FAILED_VERSIONS=$((FAILED_VERSIONS + 1))
                    local emoji="❌"
                fi
                
                local issues="0"
                if [ -f "$version_dir/build.log" ]; then
                    issues=$(grep -i "error\|exception" "$version_dir/build.log" | wc -l)
                fi
                
                echo "| $version | $emoji $status | $compilation | $mixins | $overrides | $issues |"
            fi
        done
        
        echo ""
        echo "## Summary"
        echo "- Total Versions Tested: $TOTAL_VERSIONS"
        echo "- Successful: $SUCCESSFUL_VERSIONS"
        echo "- Failed: $FAILED_VERSIONS"
        echo "- Success Rate: $((SUCCESSFUL_VERSIONS * 100 / TOTAL_VERSIONS))%"
        echo ""
        
        echo "## Details"
        echo ""
        echo "### Issues Found"
        echo ""
        
        for version in "${SUPPORTED_VERSIONS[@]}"; do
            local version_key="${version//./_}"
            local version_dir="$RESULTS_DIR/version-$version_key"
            
            if [ -f "$version_dir/status" ] && [ "$(cat "$version_dir/status")" = "FAILED" ]; then
                echo "#### Version $version"
                echo ""
                if [ -f "$version_dir/build.log" ]; then
                    echo "**Build Log Excerpts:**"
                    echo ""
                    
                    # Extract error lines
                    echo '```'
                    grep -A 3 -B 3 -i "error\|exception" "$version_dir/build.log" | head -20
                    echo '```'
                    echo ""
                fi
                
                if [ -f "$version_dir/switch.log" ]; then
                    echo "**Switch Log:**"
                    echo ""
                    echo '```'
                    cat "$version_dir/switch.log" | tail -10
                    echo '```'
                    echo ""
                fi
            fi
        done
        
        echo "### Recommendations"
        echo ""
        if [ $FAILED_VERSIONS -gt 0 ]; then
            echo "1. Investigate failed builds for the versions marked above"
            echo "2. Check dependency versions in gradle/versions/*.properties"
            echo "3. Review mixin configurations for version-specific issues"
            echo "4. Create override files for problematic versions"
        fi
        
        if [ $SUCCESSFUL_VERSIONS -eq $TOTAL_VERSIONS ]; then
            echo "✅ All versions tested successfully!"
        else
            echo "⚠️  Some versions have issues that need attention"
        fi
        
        echo ""
        echo "Generated by VulkanMod Extra Test Script"
        
    } > "$SUMMARY_FILE"
    
    print_success "Compatibility report generated: $SUMMARY_FILE"
}

# Main execution
print_status "Starting multi-version compatibility test..."
echo "Supported versions: ${SUPPORTED_VERSIONS[*]}"
echo "Results directory: $RESULTS_DIR"
echo ""

# Test each version
for version in "${SUPPORTED_VERSIONS[@]}"; do
    test_version "$version"
    echo ""
done

# Generate final report
generate_report

# Summary
print_status "Multi-version test completed!"
echo ""
echo "Results:"
echo "- Total versions tested: ${#SUPPORTED_VERSIONS[@]}"
echo "- Successful: $(grep -c "SUCCESS" "$RESULTS_DIR"/version-*/status 2>/dev/null || echo "0")"
echo "- Failed: $(grep -c "FAILED" "$RESULTS_DIR"/version-*/status 2>/dev/null || echo "0")"
echo ""
print_success "Full report available at: $SUMMARY_FILE"