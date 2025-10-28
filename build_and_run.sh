#!/bin/bash

# Script to compile and run the Library Management System

echo "Compiling Library Management System..."

# Create bin directory if it doesn't exist
mkdir -p bin

# Compile all Java files
find src -name "*.java" > sources.txt
javac -d bin @sources.txt

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Running the application..."
    java -cp bin Main
else
    echo "Compilation failed!"
fi

# Clean up
rm sources.txt