package com.trafi.state

interface State<out T : State<T, E>, in E> {
    fun reduce(event: E): T
}

interface StateListener<T : State<T, E>, in E> {
    fun onStateUpdated(oldState: T, newState: T)
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
