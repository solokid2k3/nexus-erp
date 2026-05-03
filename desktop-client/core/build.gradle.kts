plugins {
    `java-library`
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
    api("org.kordamp.ikonli:ikonli-javafx:12.4.0")
    // Primary: Bytedance IconPark — 2000+ modern vector icons
    api("org.kordamp.ikonli:ikonli-bytedance-pack:12.4.0")
    // Fallback: MaterialDesign2 for supplementary icons
    api("org.kordamp.ikonli:ikonli-materialdesign2-pack:12.4.0")
    api("org.slf4j:slf4j-api:2.0.13")
}
