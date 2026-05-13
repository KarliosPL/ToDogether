plugins {
    application
}

application {
    mainClass.set("todotool.server.Server")
}

dependencies {
    implementation(project(":shared"))
}