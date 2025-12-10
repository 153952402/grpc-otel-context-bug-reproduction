#!/bin/bash

# Test script for reproducing the gRPC Context propagation bug
# This script uses grpcurl to call the downstream service

echo "=========================================="
echo "Testing gRPC Context Propagation Bug"
echo "=========================================="
echo ""
echo "Make sure the application is running on ports 9090 and 9091"
echo ""

# Call the downstream service
echo "Calling downstream service (port 9090)..."
grpcurl -plaintext \
  -import-path ./src/main/resources/proto \
  -proto downstream_service.proto \
  -d '{"id":"test-123","data":"test data"}' \
  127.0.0.1:9090 \
  com.example.reproduction.downstream.grpc.DownstreamService/Handle

echo ""
echo "=========================================="
echo "Test completed. Check the application logs for:"
echo "1. First upstream call (synchronous) - should succeed"
echo "2. gRPC Context in async thread - should NOT be ROOT (this is the bug)"
echo "3. Second upstream call (asynchronous) - may fail due to bug"
echo "=========================================="

