plugins {
    `java-library`
}

dependencies {
    // These are inherited from root but defined here for completeness
    api("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
    api("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    api("org.kordamp.ikonli:ikonli-fontawesome5-pack:12.3.1")
    api("org.kordamp.ikonli:ikonli-material2-pack:12.3.1")
    api("org.slf4j:slf4j-api:2.0.13")
}
