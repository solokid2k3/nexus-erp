plugins {
    application
}

dependencies {
    implementation(project(":core"))
}

application {
    mainClass.set("com.erp.inventory.Launcher")
    
}
