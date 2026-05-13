plugins {
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "2.25.0"
}

application {
    mainClass.set("todotool.client.Client")
}

javafx {
    version = "21.0.6"
    modules = listOf(
        "javafx.controls",
        "javafx.fxml"
    )
}

dependencies {
    implementation(project(":shared"))
}

jlink {
    imageZip.set(
        layout.buildDirectory.file(
            "/distributions/app-${javafx.platform.classifier}.zip"
        )
    )

    options.set(
        listOf(
            "--strip-debug",
            "--compress", "2",
            "--no-header-files",
            "--no-man-pages"
        )
    )

    launcher {
        name = "app"
    }
}