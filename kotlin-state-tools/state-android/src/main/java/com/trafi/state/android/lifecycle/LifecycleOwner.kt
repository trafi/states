package com.trafi.state.android.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.trafi.state.State
import com.trafi.state.StateListener
import com.trafi.state.StateMachine

typealias OnStateUpdate<T> = (boundState: T?, newState: T) -> Unit

fun <T : State<T, E>, E> StateMachine<T, E>.subscribeWithAutoDispose(
    lifecycleOwner: LifecycleOwner,
    onUpdate: OnStateUpdate<T>
) {
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
