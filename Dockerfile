# syntax=docker/dockerfile:1.5
FROM eclipse-temurin:21-jdk

WORKDIR /app

RUN apt-get update && apt-get install -y \
    maven xvfb \
    libx11-6 libxext6 libxrender1 libxtst6 libxi6 \
    libxrandr2 libxinerama1 libxcursor1 libxdamage1 \
    libgtk-3-0 libglib2.0-0 \
    libatk1.0-0 libatk-bridge2.0-0 \
    libdrm2 libgbm1 \
    libasound2t64 \
    libfreetype6 libfontconfig1 libcairo2 \
    libpango-1.0-0 libpangocairo-1.0-0 \
    libjpeg-turbo8 libpng16-16 \
    && rm -rf /var/lib/apt/lists/*

COPY pom.xml /app/pom.xml

# cache Maven entre builds
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests dependency:go-offline

COPY . /app

RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -DskipTests package

CMD ["bash", "Docker/entrypoint.sh"]