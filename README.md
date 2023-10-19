[![Build App](https://github.com/couchbase-examples/android-java-cblite-userprofile-query/actions/workflows/build-workflow.yml/badge.svg)](https://github.com/couchbase-examples/android-java-cblite-userprofile-query/actions/workflows/build-workflow.yml)

# Quickstart in Couchbase Lite with Android and Java 
#### Build an Android App in Java with Couchbase Lite 

> This repo is designed to show you an app that allows users to log in and make changes to their user profile information.  User profile information is persisted as a Document in the local Couchbase Lite Database. When the user logs out and logs back in again, the profile information is loaded from the Database. 

Full documentation can be found on the [Couchbase Developer Portal](https://developer.couchbase.com/tutorial-quickstart-android-java-basic/).


## Prerequisites
To run this prebuilt project, you will need:

- [Android Studio Chimpmuck or above](https://developer.android.com/studio)
- Android device or emulator running API level 23 or above 
- Android SDK installed and setup (> v.34.0.0)
- Android Build Tools (> v.34.0.0)
- JDK 17 (now embedded into Android Studio)

### Installing Couchbase Lite Framework

- This project already contains the appropriate additions for downloading and utilizing the Android Couchbase Lite dependency module. However, in the future, to include Couchbase Lite support within an Android app add the following within the gradle settings file (src/gradle.settings)

```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url "https://mobile.maven.couchbase.com/maven2/dev/" } // << add this repository
        google()
        mavenCentral()
    }
}
```
* next, be sure you are using the most recent release of Couchbase Lite Mobile for Android. In the module build file (src/app/build.gradle) check this line to verify that the version number is correct:

```
dependencies {
    ...

    implementation 'com.couchbase.lite:couchbase-lite-android-ee:3.1.2'
}
```

## App Architecture

The sample app follows the [MVP pattern](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93presenter), separating the internal data model, from a passive view through a presenter that handles the logic of our application and acts as the conduit between the model and the view

## Try it out

* **Open src/build.gradle using Android Studio.**
* Build and run the project.
* Verify that you see the login screen.

## Conclusion

Setting up a basic Android app in Java with Couchbase Lite is fairly simple, this project when run will allow a user to login, update a user profile, and save the information into the embedded database.
