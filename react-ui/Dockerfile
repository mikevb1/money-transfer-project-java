FROM --platform=linux/amd64 node:20 AS build

# set the working directory inside the container
WORKDIR /web-build

# copy package files and install dependencies
COPY package*.json .env
COPY . .

# Available environment variables
ARG VITE_APP_BACKEND_URL

# Wire the variables
ENV VITE_APP_BACKEND_URL=$VITE_APP_BACKEND_URL

# Build the application
RUN npm install
RUN npm run build

# Step 2: Serve the app using NGINX
FROM --platform=linux/amd64 twalter/openshift-nginx:stable

# Expose port 8081
EXPOSE 8081

# Switch to root user to set permissions
USER root

# Copy the build output to the NGINX html directory
COPY --from=build /web-build/dist /usr/share/nginx/html

# Copy the nginx configuration file based on the environment
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY nginx/env.sh /usr/local/bin/env.sh

# Copy the script for replacing environment variables
COPY --from=build /web-build/dist/vite-envs.sh /usr/local/bin/vite-envs.sh

# Ensure the script is executable
RUN chmod +x /usr/local/bin/env.sh
RUN chmod +x /usr/local/bin/vite-envs.sh

# Ensure the NGINX configuration directory is writable
RUN chmod -R 777 /etc/nginx

RUN chmod -R 777 /usr/share/nginx/html

# Switch back to the default nginx user
USER nginx

# Set the entrypoint
ENTRYPOINT ["/usr/local/bin/env.sh"]