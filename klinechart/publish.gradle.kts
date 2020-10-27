plugins {
    `maven-publish`
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.crypto"
            artifactId = "KLineChartAndroid"
            version = "1.0.0"
        }
    }
}