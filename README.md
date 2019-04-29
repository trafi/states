# FAQ about States

- [🤔 **Why** should I use states?](#why-should-i-use-states)
- [🖼 **What** is a state?](#what-is-a-state)
- [👨‍🎨 **How** do I write states?](#how-do-i-write-states)
  - [⚡️ What can be an **event**?](#what-can-be-an-event)
  - [📣 What are **outputs**?](#what-are-outputs)
  - [🗃 What to **store privately**?](#what-to-store-privately)
  - [⚖️ What does the **reducer** do?](#what-does-the-reducer-do)
- [👨‍🔬 **How** do I write specs?](#how-do-i-write-specs)
- [👨‍🔧 **How** do I use states?](#how-do-i-use-states)

## Graph

<big><pre>
╔══════════════════════════╗
║          [**STATE**](#how-do-i-write-states)           ║
╟──────────────────────────╢
║ [Reducer](#what-does-the-reducer-do) ◀───▶ [Properties](#what-to-store-privately) ║
║    ▲               │     ║
║    │               ▼     ║
║  [Events](#what-can-be-an-event)         [Outputs](#what-are-outputs)  ║
╚════╪═══════════════╪═════╝
·   ─┼─             ─┼─
·    │  ┌─────────┐  │
·    ├─┼┼◀ [**TESTS**](#how-do-i-write-specs) ◀┼┼─┤
·    │  └─────────┘  │
·   ═╪═             ═╪═
╔════╪═══════════════╪═════╗
║    └─◀ [Feedback ↻](#how-do-i-use-states) ◀┘     ║
╟──────────────────────────╢
║        [**CONTROLLER**](#how-do-i-use-states)        ║
╚══════════════════════════╝
</pre></big>

## Why should I use states?

We use states in Trafi for a few reasons:
- States break down big problems to small pieces
- States keep our code pure and easily testable
- States help us share solutions between platforms
- State make debugging easier with a single pure function

## Why shouldn't I use states?
- It's an unusual development flow
- Overhead for very simple cases
- Takes time and practice to integrate into existing code

## What is a state?

A state is the crucial part of describing a screen's logic. It's a simple type with three main parts:
1. Privately stored data
2. Enum of events to create new state
3. Computed outputs to be handled by the controller

<details>
<summary>🔎 <i>See a simple example</i></summary>

#### iOS
```swift
struct CoinState {

  // 1. Privately stored data
  private var isHeads: Bool = true
  
  // 2. Enum of events
  enum Event {
    case flipToHeads
    case flipToTails
  }
  // .. to create new state
  static func reduce(state: CoinState, event: Event) -> CoinState {
    switch event {
    case .flipToHeads: return CoinState(isHeads: true)
    case .flipToTails: return CoinState(isHeads: false)
    }
  }
  
  // 3. Computed outputs to be handled by the controller
  var coinSide: String {
    return isHeads ? "Heads" : "Tails"
  }
}
```

#### Android
```kotlin

data class CoinState(
    // 1. Privately stored data
    private val isHeads: Boolean = true
)

// 2. Enum of events
sealed class Event {
    object FlipToHeads : Event()
    object FlipToTails : Event()
}

// .. to create new state
fun CoinState.reduce(event: Event) = when(event) {
    FlipToHeads -> copy(isHeads = true)
    FlipToTails -> copy(isHeads = false)
}
  
// 3. Computed outputs to be handled by the controller
val CoinState.coinSide: String get() {
    return isHeads ? "Heads" : "Tails"
}

```

</details>

#### Examples
- [StopState.swift](https://github.com/trafi/trafi-publictransport-ios/blob/develop/PublicTransport/Stop/StopState.swift)
- [SearchState.swift](https://github.com/trafi/trafi-publictransport-ios/blob/develop/PublicTransport/Search/SearchState.swift)
- [MyCommuteState.swift](https://github.com/trafi/trafi-ios/blob/feature/my-commute/trafi/Code/MyCommute/MyCommuteState.swift) and [MyCommuteState.kt](https://github.com/trafi/trafi-android/blob/my-commute/trafi/src/main/java/com/trafi/android/ui/mycommute/MyCommuteState.kt)
- [MyCommuteEditState.swift](https://github.com/trafi/trafi-ios/blob/feature/my-commute/trafi/Code/MyCommute/MyCommuteEdit/MyCommuteEditState.swift) and [MyCommuteEditState.kt](https://github.com/trafi/trafi-android/blob/my-commute/trafi/src/main/java/com/trafi/android/ui/mycommute/edit/MyCommuteEditState.kt)

## How do I write states?
There are many ways to write states. We can recommend following these steps:
- Draft a platform-independent interface:
  - List events that could happen
  - List outputs to display UI, load data and navigate
- Implement the internals:
  - ❌ Write a failing test that sends an event and asserts an output
  - ✅ Add code to state till test passes
  - 🛠 Refactor code so it's nice, but all tests still pass
  - 🔁 Continue writing tests for all events and outputs

### What can be an event?
Anything that just happened that the state should know about is an event. Events can be easily understood and listed by non-developers. Most events come from a few common sources:
- User interactions
  - `tappedSearch`
  - `tappedResult`
  - `completedEditing`
  - `pulledToRefresh`
- Networking
  - `loadedSearchResults`
  - `loadedMapData`
- Screen lifecycle
  - `becameReadyForRefresh`
  - `becameVisible`
  - `enteredBackground`
- Device
  - `wentOffline`
  - `changedCurrentLocation`

As events are something that just happened we start their names with verbs in past simple tense.

<details>
<summary>🔎 <i>See an example</i></summary>
  
#### iOS
```swift
struct MyCommuteState {
  enum Event {
    case refetched(MyCommuteResponse)
    case wentOffline
    case loggedIn(Bool)
    case activatedTab(index: Int)
    case tappedFavorite(MyCommuteTrackStopFavorite)
    case tappedFeedback(MyCommuteUseCase, MyCommuteFeedbackRating)
    case completedFeedback(String)
  }
}
```

#### Android
```kotlin
data class MyCommuteState(/**/)

sealed class Event {
    data class Refetched(val response: MyCommuteResponse) : Event()
    object WentOffline : Event()
    data class LoggedIn(val isLoggedIn: Boolean) : Event()
    data class ActivatedTab(val index: Int) : Event()
    data class TappedFavorite(val favorite: MyCommuteTrackStopFavorite) : Event()
    data class TappedFeedback(val feedback: Feedback) : Event()
    data class CompletedFeedback(val message: String) : Event()
}
```

</details>

### What are outputs?
Outputs are the exposed getters of state. Controllers listen to state changes through outputs. Like events, outputs are simple enough to be understood and listed by non-developers. Most outputs can be categorized as:
- _UI_. These are usually non-optional outputs that specific UI elements are bound to, e.g:
  - `isLoading: Bool`
  - `paymentOptions: [PaymentOption]`
  - `profileHeader: ProfileHeader`
- _Data_. These are usually optional outputs that controllers react to. Their names indicate how to react and their types give associated information if needed, e.g:
  - `loadAutocompleteResults: String?`
  - `loadNearbyStops: LatLng?`
  - `syncFavorites: Void?`
- _Navigation_. These are always optional outputs that are just proxies for navigation, e.g.:
  - `showStop: StopState?`
  - `showProfile: ProfileState?`
  - `dismiss: Void?`

### What to store privately?
Any properties that are needed to produce the necessary outputs can be stored privately. We strive for this to be the minimal ground truth needed to represent any possible valid state.

<details>
<summary>🔎 <i>See an example</i></summary>
  
#### iOS
```swift
struct PhoneVerificationState {
    private let phoneNumber: String
    private var waitBeforeRetrySeconds: Int
}
```

#### Android
```kotlin
data class PhoneVerificationState(
    private val phoneNumber: String,
    private val waitBeforeRetrySeconds: Int
)
```

</details>

### What does the reducer do?
The reducer is a pure function that changes the state's privately stored properties according to an event.

<details>
<summary>🔎 <i>See an example</i></summary>
  
#### iOS
```swift
struct CoinState {
    private var isHeads: Bool = true

    static func reduce(_ state: CoinState, event: Event) -> CoinState {
        var result = state
        switch event {
            case .flipToHeads: result.isHeads = true
            case .flipToTails: result.isHeads = false
        }
        return result
    }
}
```

#### Android
```kotlin
data class CoinState(private val isHeads: Boolean) {

    fun reduce(event: Event) = when(event) {
        FlipToHeads -> copy(isHeads = true)
        FlipToTails -> copy(isHeads = false)
    }
}
```

</details>


## How do I write specs?
[WIP] In a BDD style. For iOS we use [`Quick` and `Nible`](https://github.com/Quick/Quick), for Android [`Spek`](https://github.com/spekframework/spek).

## How do I use states?
[WIP] iOS uses states in a reactive way using [RxFeedback](https://github.com/NoTests/RxFeedback.swift).
