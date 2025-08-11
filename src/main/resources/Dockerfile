# ==========================
# ⚙️ Etapa 1: Build (Gradle)
# ==========================
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# ⏱️ Cache de dependências do Gradle
# (Aproveita cache quando só código muda)
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
# Se usar Kotlin DSL, troque as linhas acima por build.gradle.kts/settings.gradle.kts
# COPY build.gradle.kts build.gradle.kts
# COPY settings.gradle.kts settings.gradle.kts

# Copia apenas arquivos de configuração do Gradle (se existirem)
COPY gradle gradle
COPY gradlew gradlew
COPY gradle.properties gradle.properties

# Resolve dependências (sem código do app ainda)
RUN gradle --no-daemon dependencies || true

# Agora copia o restante do projeto
COPY . .

# Gera o JAR (pula testes no build de imagem)
RUN gradle --no-daemon clean bootJar -x test


# ==========================================
# 🐳 Etapa 2: Runtime leve com FFmpeg + JRE
# ==========================================
FROM eclipse-temurin:17-jre-alpine

# GC e limites de memória seguros em contêiner
ENV JAVA_TOOL_OPTIONS="-XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
# Fuso horário (opcional)
ENV TZ=America/Sao_Paulo

# Instala FFmpeg (necessário pro HLS) e tzdata
RUN apk add --no-cache ffmpeg tzdata ca-certificates && update-ca-certificates

WORKDIR /app

# Copia o jar gerado
COPY --from=builder /app/build/libs/*.jar app.jar

# Segurança: roda como usuário não-root
RUN addgroup -S app && adduser -S app -G app
USER app

# Porta padrão (Render normalmente usa $PORT; seu app já lê SERVER_PORT)
ENV SERVER_PORT=8086
EXPOSE ${SERVER_PORT}

# Healthcheck simples (opcional; ajuste rota se tiver /actuator/health)
# HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
#   CMD wget -qO- "http://127.0.0.1:${SERVER_PORT}/health" || exit 1

# Inicia a aplicação
ENTRYPOINT ["java","-jar","/app/app.jar"]
