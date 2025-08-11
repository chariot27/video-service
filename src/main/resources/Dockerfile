# ==========================
# ⚙️ Etapa 1: Build (Gradle)
# ==========================
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Cache de dependências (não exige gradle.properties)
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
# Se usar Kotlin DSL, comente a linha acima e descomente estas:
# COPY build.gradle.kts build.gradle.kts
# COPY settings.gradle.kts settings.gradle.kts

COPY gradle gradle
COPY gradlew gradlew
RUN chmod +x gradlew

# Resolve deps sem código ainda (ignora falha de árvore opcional)
RUN ./gradlew --no-daemon dependencies || true

# Agora copia o projeto inteiro
COPY . .

# Build do jar (pula testes)
RUN ./gradlew --no-daemon clean bootJar -x test

# ==========================================
# 🐳 Etapa 2: Runtime leve com FFmpeg + JRE
# ==========================================
FROM eclipse-temurin:17-jre-alpine
ENV JAVA_TOOL_OPTIONS="-XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV TZ=America/Sao_Paulo

RUN apk add --no-cache ffmpeg tzdata ca-certificates && update-ca-certificates

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Usuário não-root
RUN addgroup -S app && adduser -S app -G app
USER app

ENV SERVER_PORT=8086
EXPOSE ${SERVER_PORT}

ENTRYPOINT ["java","-jar","/app/app.jar"]
