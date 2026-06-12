# =========================
# RUNTIME (完整 JRE，jar 由宿主機 mvnw package 先打好)
# =========================
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=local
ENTRYPOINT ["java","-jar","app.jar"]
