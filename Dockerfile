# /home/frozzel/IdeaProjects/SpringReviewer/Dockerfile

# --- Стадия сборки ---
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
# Копируем pom.xml
COPY pom.xml .
# Загружаем зависимости
RUN mvn dependency:go-offline -B
# Копируем исходный код
COPY src ./src
# Собираем приложение, ПОЛНОСТЬЮ ПРОПУСКАЯ КОМПИЛЯЦИЮ И ЗАПУСК тестов
RUN mvn package -Dmaven.test.skip=true

# --- Стадия выполнения ---
# Используем рекомендованный образ JRE 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} application.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "application.jar"]