# Kotlin state tools

```kotlin
interface State<out T : State<T, E>, in E> {
    fun reduce(event: E): T
}
```

See the [phone verification sample](/samples/phone-verification).

## Installation

```groovy
// top-level build.gradle
allprojects {
    repositories {
        // ..
        maven { url 'https://jitpack.io' }
    }
}

// module build.gradle
dependencies {
    // ..
    implementation 'com.trafi.states:state:master-SNAPSHOT'
    implementation 'com.trafi.states:state-android:master-SNAPSHOT'
}
```
