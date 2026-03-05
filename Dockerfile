# ---- runtime ----
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 时区（可选）
ENV TZ=Asia/Shanghai

# 把 jar 拷进去（建议你把最终 jar 命名为 app.jar）
COPY target/*.jar /app/app.jar

# Spring Boot 常用端口（你项目默认 8080）
EXPOSE 8080

# JVM 参数可按需调；-XX:MaxRAMPercentage 让容器内更稳
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]