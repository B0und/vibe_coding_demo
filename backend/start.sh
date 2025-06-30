#!/bin/bash
echo "=== Starting Backend Application ==="

# Use the JAR file copied as app.jar
JAR_FILE="app.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "ERROR: JAR file not found: $JAR_FILE"
    exit 1
fi

echo "Found JAR file: $JAR_FILE"
echo "Starting application..."
java -jar "$JAR_FILE" 