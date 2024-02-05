job("Build and publish") {
    container(displayName = "Run publish script", image = "gradle:jdk8") {
        kotlinScript { api ->
            api.gradlew("build")
            api.gradlew("publish")
        }
    }
}