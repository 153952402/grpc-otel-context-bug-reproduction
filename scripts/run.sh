#!/bin/bash

# Script to run the application

echo "=========================================="
echo "Starting gRPC OpenTelemetry Context Bug Reproduction"
echo "=========================================="
echo ""
echo "The application will start:"
echo "  - Downstream gRPC service on port 9090"
echo "  - Upstream gRPC service on port 9091"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

cd "$(dirname "$0")/.."
mvn spring-boot:run

