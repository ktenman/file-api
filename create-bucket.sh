#!/bin/sh

# Install MinIO Client (mc)
apk add --no-cache curl
curl -sSLo /usr/bin/mc https://dl.min.io/client/mc/release/linux-amd64/mc
chmod +x /usr/bin/mc

# Configure mc with MinIO server
mc alias set myminio http://minio:9000 minioaccesskey miniosecretkey

# Check MinIO readiness
until mc admin info myminio; do
  echo "Waiting for MinIO to be available..."
  sleep 1
done

# Create the bucket
mc mb myminio/mybucket

echo "Bucket created successfully"
