job("Build and run tests") {
    container(displayName = "Run gradle build", image = "amazoncorretto:17-alpine") {
        kotlinScript { api ->
            api.gradlew("publish")
        }
    }
}