#!/bin/sh
# Movie Backend - Start Script
# Usage: ./start.sh

MAVEN_HOME="C:/Users/songw/AppData/Local/Temp/maven/apache-maven-3.9.6"
PROJECT_DIR="$( cd "$( dirname "$0" )" && pwd )"

cd "$PROJECT_DIR"

echo ">>> Building project..."
"$MAVEN_HOME/bin/mvn" package -DskipTests -Dhttps.protocols=TLSv1.2 -q 2>&1

if [ $? -ne 0 ]; then
    echo "BUILD FAILED!"
    exit 1
fi

echo ">>> Starting application..."
echo ">>> Server will start at http://localhost:8080"
echo ""

"$MAVEN_HOME/bin/mvn" spring-boot:run -Dhttps.protocols=TLSv1.2 2>&1
