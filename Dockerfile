# [AI] - START
# ~~~~~~~~~~~~~~~ Build from source, then layered JARs ~~~~~~~~~~~~~~~
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY mvnw pom.xml ./
COPY .mvn/ .mvn/
COPY src/ src/
# Cache mount keeps the Maven repo between builds, so dependencies download once.
# Tests are skipped here; run them with ./mvnw test, not during the image build.
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B package -Dmaven.test.skip=true \
    && java -Djarmode=layertools -jar target/*.jar extract --destination /application

FROM eclipse-temurin:17-jre
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring
WORKDIR /application
COPY --from=build /application/dependencies/ ./
COPY --from=build /application/spring-boot-loader/ ./
COPY --from=build /application/snapshot-dependencies/ ./
COPY --from=build /application/application/ ./
EXPOSE 8085
ENTRYPOINT ["java", "-cp", ".", "org.springframework.boot.loader.launch.JarLauncher"]
# [AI] - END
