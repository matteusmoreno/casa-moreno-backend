# Etapa 1: build da aplicação usando Maven
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: imagem final, apenas com o jar
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o jar gerado para a imagem final
COPY --from=build /app/target/casa-moreno-backend-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta configurada na aplicação
EXPOSE 8085

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
