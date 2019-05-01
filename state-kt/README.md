# State for Kotlin

```kotlin
interface State<out T : State<T, E>, in E> {
    fun reduce(event: E): T
}
```

See a sample implementation in the [`phoneverification` folder](phoneverification).

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
