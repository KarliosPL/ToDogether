plugins {
    application
}

application {
    mainClass.set("todotool.server.Server")
}

dependencies {
    implementation(project(":shared"))
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
}