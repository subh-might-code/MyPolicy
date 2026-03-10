# ----------------------------
# Stage 1: Build Flutter Web
# ----------------------------
FROM ghcr.io/cirruslabs/flutter:3.41.1 AS build


WORKDIR /app

# Copy project files into container
COPY . .

# Enable web support
RUN flutter config --enable-web

# Get dependencies
RUN flutter pub get

# Build web in release mode, pointing API base URL to BFF service inside Docker network
RUN flutter build web --release --dart-define=API_BASE_URL=http://bff-service:8090


# ----------------------------
# Stage 2: Serve with Nginx
# ----------------------------
FROM nginx:alpine

COPY --from=build /app/build/web /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
