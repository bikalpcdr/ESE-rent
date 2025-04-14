#!/bin/bash

# Export session secret for Spring Security
export SESSION_SECRET="development-session-secret"
export JAVA_TOOL_OPTIONS="-Xmx512m"

# Display database info
echo "Using PostgreSQL database:"
echo "Host: $PGHOST"
echo "Port: $PGPORT"
echo "Database: $PGDATABASE"
echo "User: $PGUSER"
echo "Connection URL: $DATABASE_URL"

# Build the application
echo "Building the application..."
cd CarRentalSystem
mvn -Dmaven.test.skip=true clean package

# Run the application
echo "Starting the Room Rental Application..."
java -jar target/eserent-0.0.1-SNAPSHOT.jar