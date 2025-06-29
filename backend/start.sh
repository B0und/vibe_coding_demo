#!/bin/bash
echo "=== Starting Backend Application ==="
echo "Contents of build/libs directory:"
ls -la build/libs/
echo ""

# Find the JAR file (exclude -plain.jar files)
JAR_FILE=$(find build/libs -name "*.jar" -type f ! -name "*-plain.jar" | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "ERROR: No JAR file found in build/libs/"
    exit 1
fi

echo "Found JAR file: $JAR_FILE"
echo "Starting application..."
java -jar "$JAR_FILE" 