# FAQ about State

- [🤔 **Why** should I use states?](#why-should-i-use-states)
- [🖼 **What** is a state?](#what-is-a-state)
- [👨‍🎨 **How** do I write states?](#how-do-i-write-states)
  - [What can be an **event**?](#what-can-be-an-event)
  - [What are **queries**?](#what-are-queries)
  - [What to **store privately**?](#what-to-store-privately)
  - [What does the **reducer** do?](#what-does-the-reducer-do)
  - [What about **commands**?](#what-about-commands)

---

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
<table>
<tr><th>iOS</th><th>Android</th></tr>
<tr>
<td>

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
  static func reduce(state: CoinState,
                     event: Event) -> CoinState {
    switch event {
    case .flipToHeads:
      return CoinState(isHeads: true)
    case .flipToTails:
      return CoinState(isHeads: false)
    }
  }
  
  // 3. Computed queries to read stored data
  var coinSide: String {
    return isHead ? "Heads" : "Tails"
  }
}
```

</td>
<td>

```kotlin
data class CoinState {

  // 1. Privately stored data
  
  // 2. Enum of events
  // .. to create new state
  
  // 3. Computed queries to read stored data
}
```

</td>
</tr>
</table>
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

It's common for events to carry related information.

### What are queries?
Things you want to know about state

### What to store privately?
Information used to answer queries

### What does the reducer do?
Changes privately stored info according to the event

### What about commands?
They're the returned side effects not persisted in state
