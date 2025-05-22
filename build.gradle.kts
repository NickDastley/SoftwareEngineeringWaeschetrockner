plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.example.Main")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}
