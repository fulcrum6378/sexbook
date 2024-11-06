pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/fulcrum6378/HelloCharts")
            credentials {
                username = "fulcrum6378"
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/fulcrum6378/mcdtp")
            credentials {
                username = "fulcrum6378"
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

rootProject.name = "Sexbook"
include(":app")
