job("Build and publish") {
    container(displayName = "Run publish script", image = "gradle") {
        kotlinScript { api ->
            api.gradlew("build")
            api.gradlew("publish")
        }
    }
}