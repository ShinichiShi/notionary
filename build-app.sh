#!/bin/bash

# Notionary Build Script
echo "======================================"
echo "Notionary - Clean Build Script"
echo "======================================"
echo ""

# Step 1: Clean old builds
echo "Step 1: Cleaning old builds..."
rm -rf .gradle/ 2>/dev/null
rm -rf app/build/ 2>/dev/null
rm -rf build/ 2>/dev/null
echo "✓ Cleaned old builds"
echo ""

# Step 2: Clean with Gradle
echo "Step 2: Running Gradle clean..."
./gradlew clean --no-configuration-cache
echo "✓ Gradle clean completed"
echo ""

# Step 3: Sync dependencies
echo "Step 3: Syncing dependencies..."
./gradlew --refresh-dependencies --no-configuration-cache
echo "✓ Dependencies synced"
echo ""

# Step 4: Build the app
echo "Step 4: Building the app..."
./gradlew assembleDebug --no-configuration-cache
echo "✓ Build completed"
echo ""

echo "======================================"
echo "Build process completed successfully!"
echo "======================================"
echo ""
echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
echo ""

