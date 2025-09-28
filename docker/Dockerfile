### web前端构建阶段 ###
FROM node:22 AS web-build-stage
WORKDIR /app


COPY NeoKG-front/ ./

# 优化git clone，减少下载大小
RUN npm i && \
    npm run build

### 后端构建阶段 ###
FROM eclipse-temurin:25-jdk-jammy AS backend-build-stage
WORKDIR /app

COPY NeoKG-backend/ ./

# 从前端构建阶段复制构建结果到后端静态资源目录
COPY --from=web-build-stage /app/dist /app/src/main/resources/static

# 构建项目
RUN ./mvnw package -DskipTests

### 生产阶段 ###
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

# 从构建阶段复制构建结果
COPY --from=backend-build-stage /app/target/NeoKG-backend-0.0.1-SNAPSHOT.jar ./app.jar

# 暴露应用端口(默认Spring Boot端口)
EXPOSE 8080

# 允许通过环境变量传递JVM参数
ENV JAVA_OPTS=""

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]