plugins {
    java
    id("org.openjfx.javafxplugin") version "0.1.0" apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.openjfx.javafxplugin")

    group = "com.erp.desktop"
    version = "1.0.0"

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    configure<org.openjfx.gradle.JavaFXOptions> {
        version = "21.0.2"
        modules("javafx.controls", "javafx.fxml")
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        "implementation"("com.fasterxml.jackson.core:jackson-databind:2.17.2")
        "implementation"("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
        "implementation"("org.kordamp.ikonli:ikonli-javafx:12.4.0")
        // Primary: Bytedance IconPark (2000+ modern icons)
        "implementation"("org.kordamp.ikonli:ikonli-bytedance-pack:12.4.0")
        // Fallback: MaterialDesign2
        "implementation"("org.kordamp.ikonli:ikonli-materialdesign2-pack:12.4.0")
        "implementation"("org.slf4j:slf4j-api:2.0.13")
        "implementation"("ch.qos.logback:logback-classic:1.5.6")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}
