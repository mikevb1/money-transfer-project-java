#!/bin/sh

# Load environment variables from the .env file if it exists
if [ -f /usr/local/bin/.env ]; then
    export "$(grep -v '^#' /usr/local/bin/.env | xargs)"
fi

# Debugging: print environment variables
echo "VITE_APP_BACKEND_URL=${VITE_APP_BACKEND_URL}"


# Replace placeholders in the Nginx configuration file with environment variables
envsubst "${VITE_APP_BACKEND_URL}" < /etc/nginx/nginx.conf > /etc/nginx/nginx.conf.tmp

# Check the contents of the temporary nginx configuration file for debugging
cat /etc/nginx/nginx.conf.tmp

# Check if the temp file was created successfully before moving it
if [ -f /etc/nginx/nginx.conf.tmp ]; then
    mv /etc/nginx/nginx.conf.tmp /etc/nginx/nginx.conf
else
    echo "Failed to create /etc/nginx/nginx.conf.tmp"
    exit 1
fi

# Check if the env script is present & run it
if [ -f /usr/share/nginx/html/vite-envs.sh ]; then
  sh /usr/share/nginx/html/vite-envs.sh
else
    echo "Failed to run /usr/share/nginx/html/vite-envs.sh"
    exit 1
fi

# Start nginx
nginx -g 'daemon off;'