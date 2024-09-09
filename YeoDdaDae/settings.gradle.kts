pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://devrepo.tmapadmin.com/repository/tmap-sdk-release/")
        maven("https://jitpack.io")
    }
}

rootProject.name = "YeoDdaDae"
include(":app")
