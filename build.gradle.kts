plugins {
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
}

application {
    mainClass.set("com.jagex.runescape.Client")
}

sourceSets {
    main {
        java {
            srcDir("src")
        }
    }
}

