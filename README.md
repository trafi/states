[⚡️ Lightning talk intro to states](https://speakerdeck.com/justasm/correct-ui-logic-with-state-machines)

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
- States help us share solutions between platforms & languages
- States make debugging easier with a single pure function

## Why shouldn't I use states?
- It's an unusual development flow
- Overhead for very simple cases
- Takes time and practice to integrate into existing code

## What is a state?

A state is the brains of a screen. It makes all the important decisions. It's a simple type with three main parts:
1. Privately stored data
2. Enum of events to create new state
3. Computed outputs to be handled by the controller

<details>
<summary>🔎 <i>See a simple example</i></summary>

#### Swift
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

#### Kotlin
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

#### Samples
- [Language selection](samples/language-selection)
- [Payment method details](samples/payment-method-details)

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
  
#### Swift
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

#### Kotlin
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
Any properties that are needed to compute the necessary outputs can be stored privately. We strive for this to be the minimal ground truth needed to represent any possible valid state.

<details>
<summary>🔎 <i>See an example</i></summary>
  
#### Swift
```swift
struct PhoneVerificationState {
    private let phoneNumber: String
    private var waitBeforeRetrySeconds: Int
}
```

#### Kotlin
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
  
#### Swift
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

#### Kotlin
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
We write specs (tests) in a BDD style. For Swift we use [`Quick` and `Nible`](https://github.com/Quick/Quick), for Kotlin [`Spek`](https://github.com/spekframework/spek).

<details>
<summary>🔎 <i>See an example</i></summary>
  
#### Swift
```swift
class MyCommuteSpec: QuickSpec {

    override func spec() {

        var state: MyCommuteState!
        beforeEach {
            state = .initial(response: .dummy, now: .h(10))
        }

        context("When offline") {

            it("Has no departues") {
                expect(state)
                    .after(.wentOffline)
                    .toTurn { $0.activeFavorites.flatMap { $0.departures }.isEmpty }
            }

            it("Has no disruptions") {
                expect(state)
                    .after(.wentOffline)
                    .toTurn { $0.activeFavorites.filter { $0.severity != .notAffected }.isEmpty }
            }
        }
    }
}
```

#### Kotlin
```kotlin
object NearbyStopsStateSpec : Spek({
    describe("Stops near me") {

        describe("when location is present") {
            var state = NearbyStopsState(hasLocation = true)
            beforeEach { state = NearbyStopsState(hasLocation = true) }

            describe("at start") {
                it("shows progress") { assertEquals(Ui.Progress, state.ui) }
                it("tries to load stops") { assertTrue(state.loadStops) }
            }
        }
    }
}
```

</details>


## How do I use states?
States become useful when their outputs are connected to UI, network requests, and other side effects.

Reactive streams compose nicely with the states pattern. We recommend using [RxFeedback.swift](https://github.com/NoTests/RxFeedback.swift) / [RxFeedback.kt](https://github.com/NoTests/RxFeedback.kt) to connect states to side effects in a reactive way.

<details>
<summary>🔎 <i>See an example</i></summary>
  
#### Swift
```swift
Driver.system(
        initialState: input,
        reduce: PhoneVerificationState.reduce,
        feedback: uiBindings() + dataBindings() + [produceOutput()])
    .drive()
    .disposed(by: rx_disposeBag)
```

</details>

States are versatile and can be used with more traditional patterns, e.g. observer / listener.

<details>
<summary>🔎 <i>See an example</i></summary>
  
#### Kotlin (Android)
```kotlin

private val machine = StateMachine(PhoneVerificationState("+00000000000"))

machine.subscribeWithAutoDispose(viewLifecycleOwner) { boundState, newState ->
    // do things with newState
}


// boring implementation below

typealias OnStateUpdate<T> = (boundState: T?, newState: T) -> Unit

interface StateListener<T : State<T, E>, in E> {
    fun onStateUpdated(oldState: T, newState: T)
}

interface State<out T : State<T, E>, in E> {
    fun reduce(event: E): T
}

class StateMachine<T : State<T, E>, E>(initial: T) {

    private val listeners = mutableListOf<StateListener<T, E>>()
    fun addListener(listener: StateListener<T, E>) = listeners.add(listener)
    fun removeListener(listener: StateListener<T, E>) = listeners.remove(listener)

    var state: T = initial
        private set(value) {
            val oldValue = field
            field = value
            listeners.forEach { it.onStateUpdated(oldValue, value) }
        }

    fun transition(event: E) {
        state = state.reduce(event)
    }

}

fun <T : State<T, E>, E> StateMachine<T, E>.subscribeWithAutoDispose(lifecycleOwner: LifecycleOwner,
                                                                     onUpdate: OnStateUpdate<T>) {

    val listener = object : StateListener<T, E> {
        override fun onStateUpdated(oldState: T, newState: T) = onUpdate(oldState, newState)
    }

    lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
        // addObserver will call this if lifecycle is already in STARTED state
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun start() = addListener(listener)

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun stop() = removeListener(listener)
    })

    onUpdate(null, state)
}
```

</details>
