# FAQ about State

- [🤔 **Why** should I use states?](#why-should-i-use-states)
- [🖼 **What** is a state?](#what-is-a-state)
- [👨‍🎨 **How** do I write states?](#how-do-i-write-states)
  - [⚡️ What can be an **event**?](#what-can-be-an-event)
  - [🗂 What are **queries**?](#what-are-queries)
  - [📣 What about **commands**?](#what-about-commands)
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
║  [Events](#what-can-be-an-event)         [Queries](#what-are-queries)  ║
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

We're using states in Trafi for a few reasons:
- States help us share solutions between platforms
- States break down big problems to small pieces
- States keep our code pure and easily testable

## What is a state?

State is the crucial part of describing screen's logic in Trafi. It's a simple type with three main parts:
1. Privately stored data
2. Enum of events to create new state
3. Computed queries to read stored data

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
  
  // 3. Computed queries to read stored data
  var coinSide: String {
    return isHead ? "Heads" : "Tails"
  }
}
```

#### Android
```kotlin
data class CoinState {

  // 1. Privately stored data
  
  // 2. Enum of events
  // .. to create new state
  
  // 3. Computed queries to read stored data
}
```

</details>

#### Examples
- [StopState.swift](https://github.com/trafi/trafi-publictransport-ios/blob/develop/PublicTransport/Stop/StopState.swift)
- [SearchState.swift](https://github.com/trafi/trafi-publictransport-ios/blob/develop/PublicTransport/Search/SearchState.swift)
- [MyCommuteState.swift](https://github.com/trafi/trafi-ios/blob/feature/my-commute/trafi/Code/MyCommute/MyCommuteState.swift) and [MyCommuteState.kt](https://github.com/trafi/trafi-android/blob/my-commute/trafi/src/main/java/com/trafi/android/ui/mycommute/MyCommuteState.kt)
- [MyCommuteEditState.swift](https://github.com/trafi/trafi-ios/blob/feature/my-commute/trafi/Code/MyCommute/MyCommuteEdit/MyCommuteEditState.swift) and [MyCommuteEditState.kt](https://github.com/trafi/trafi-android/blob/my-commute/trafi/src/main/java/com/trafi/android/ui/mycommute/edit/MyCommuteEditState.kt)

## How do I write states?
There're many ways how you could write them. We can recommend following these steps:
- Draft an interface:
  - List events that could happen
  - List queries to display UI and load data
  - List commands for navigation flow
- Implement the internals:
  - ❌ Write a failing test that sends an event and asserts a query
  - ✅ Add code to state till test passes
  - 🛠 Refactor code so it's nice, but all tests still pass
  - 🔁 Continue writing tests for all events and queries

### What can be an event?
Anything that just happened that state needs to know. Events can be easily understood and listed by non-developers. Most of events come from a few places:
- User interactions
  - `tappedSearch`
  - `tappedResult`
  - `completedEditing`
  - `pulledToRefresh`
- Networking
  - `loadedSearchResults`
  - `loadedMapData`
- Screen updates
  - `becameReadyForRefresh`
  - `becameVisible`
- Global changes
  - `wentOffline`
  - `willEnterBackground`
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
    case activatedTab(Int)
    case tappedFavorite(MyCommuteTrackStopFavorite)
    case tappedFeedback(MyCommuteUseCase, MyCommuteFeedbackRating)
    case completedFeedback(String)
  }
}
```

#### Android
```kotlin
data class MyCommuteState() {
  sealed class Event {
    data class Refetched(val response: MyCommuteResponse) : Event()
    object WentOffline : Event()
    data class LoggedIn(val isLoggedIn: Boolean) : Event()
    data class ActivatedTab(val index: Int) : Event()
    data class TappedFavorite(val favorite: MyCommuteTrackStopFavorite) : Event()
    data class TappedFeedback(val feedback: Feedback) : Event()
    data class CompletedFeedback(val message: String) : Event()
  }
}
```

</details>

### What are queries?
The output of state, the exposed getters. Controller listens to state changes through queries. Like events, queries are simple enough to be understood and listed by non-developers. There are a few categories that most queries can be assigned to:
- _UI_. These are usually non-optional queries that specific UI elements are binded to, e.g:
  - `isLoading: Bool`
  - `paymentOptions: [PaymentOption]`
  - `stopHeader: StopHeader`
- _Data_. These are usually optional queries that controllers react to. Their names indicate how to react and their types give associated information if needed, e.g:
  - `loadAutocompleteResults: String?`
  - `loadNearbyStops: LatLng?`
  - `syncFavorites: Void?`
- _Flow_. These are always optional queries that are just proxies for commands, e.g.:
  - `showStop: StopState?`
  - `showTrack: TrackState?`
  - `dismiss: Void?`

### What about commands?
They're the returned side effects not persisted in state

### What to store privately?
Information used to answer queries

### What does the reducer do?
Changes privately stored info according to the event


## How do I write specs?
In a BDD style. For iOS we use [`Quick` and `Nible`](https://github.com/Quick/Quick), for Android [`Spek`](https://github.com/spekframework/spek).

## How do I use states?
iOS uses states in a reactive way using [RxFeedback](https://github.com/NoTests/RxFeedback.swift).
