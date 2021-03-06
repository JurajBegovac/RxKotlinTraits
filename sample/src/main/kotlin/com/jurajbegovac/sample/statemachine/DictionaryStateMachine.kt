package com.jurajbegovac.sample.statemachine

import com.jurajbegovac.rxkotlin.traits.driver.Driver
import com.jurajbegovac.rxkotlin.traits.driver.DriverSharingStrategy
import com.jurajbegovac.rxkotlin.traits.driver.asDriverCompleteOnError
import com.jurajbegovac.rxkotlin.traits.driver.drive
import com.jurajbegovac.rxkotlin.traits.shared_sequence.*
import rx.Subscription
import rx.subjects.PublishSubject

/** Created by juraj on 23/05/2017. */

class DictionaryStateMachine<Key, State>(val effectsForKey: (Key) -> (Driver<State>) -> Driver<Command<Key, State>>) {
  private val commands: PublishSubject<Pair<Key, State>> = PublishSubject.create()
  private val stateSubscription: Subscription
  
  val state: Driver<Map<Key, State>>
  
  init {
    val userCommandsFeedback: (Driver<Map<Key, State>>) -> Driver<Command<Key, State>> = {
      this.commands.asObservable().map<Command<Key, State>> {
        Command.Update(it)
      }
          .asDriverCompleteOnError()
    }
    
    this.state = Driver.system(
        mutableMapOf<Key, State>(),
        ::reduce,
        userCommandsFeedback, perKeyFeedbackLoop(this.effectsForKey)
    )
    
    this.stateSubscription = this.state.drive()
  }
  
  fun transition(to: Pair<Key, State>) {
    this.commands.onNext(to)
  }
  
  fun dispose() {
    this.stateSubscription.unsubscribe()
  }
}

sealed class Command<out Key, out State> {
  data class Update<out Key, out State>(val state: Pair<Key, State>) : Command<Key, State>()
  data class Finish<out Key, out State>(val key: Key) : Command<Key, State>()
}

private sealed class MutationEvent<in Key, out State> {
  data class Started<Key, out State>(val state: Pair<Key, State>) : MutationEvent<Key, State>()
  data class Updated<Key, out State>(val state: Pair<Key, State>) : MutationEvent<Key, State>()
  data class Finished<Key, out State>(val key: Key) : MutationEvent<Key, State>()
  
  fun isUpdate(key: Key): Boolean = when (this) {
    is Updated -> this.state.first == key
    else -> false
  }
  
  fun isFinished(key: Key): Boolean = when (this) {
    is Finished -> this.key == key
    else -> false
  }
  
  fun state(): State? = when (this) {
    is Started -> this.state.second
    is Updated -> this.state.second
    is Finished -> null
  }
}

private fun <Key, State> reduce(state: Map<Key, State>,
                                command: Command<Key, State>): Map<Key, State> = when (command) {
  is Command.Update -> state + command.state
  is Command.Finish -> state - command.key
}

private fun <Key, State> perKeyFeedbackLoop(effects: (Key) -> (Driver<State>) -> Driver<Command<Key, State>>): (Driver<Map<Key, State>>) -> Driver<Command<Key, State>> {
  return { state ->
    val events = state.scanAndMaybeEmit(
        emptyMap<Key, State>(),
        { (oldState, newState) ->
          val oldKeys = oldState.keys
          val newKeys = newState.keys
          
          val finishedEvents: List<MutationEvent<Key, State>> = oldKeys.subtract(newKeys).map {
            MutationEvent.Finished<Key, State>(it)
          }
          val newEvents: List<MutationEvent<Key, State>> = newKeys.subtract(oldKeys).map {
            MutationEvent.Started(Pair(it, newState[it]!!))
          }
          val updatedEvents: List<MutationEvent<Key, State>> = newKeys.intersect(oldKeys).map {
            MutationEvent.Updated(Pair(it, newState[it]!!))
          }
          
          return@scanAndMaybeEmit Pair(newState, newEvents + updatedEvents + finishedEvents)
        })
        .flatMapIterable { it }
    
    events
        .flatMap { event ->
          val started = event as? MutationEvent.Started<Key, State> ?: return@flatMap DriverSharingStrategy.empty<DriverSharingStrategy, Command<Key, State>>()
          
          val keyState = started.state
          
          val statePerKey = events
              .filter { it.isUpdate(keyState.first) }
              .startWith(event)
              .flatMap { it.state()?.let { DriverSharingStrategy.just(it) } ?: DriverSharingStrategy.empty() }
              .distinctUntilChanged()
          
          return@flatMap effects(keyState.first)(statePerKey)
              .asObservable()
              .takeUntil(events.filter { it.isFinished(keyState.first) }.asObservable())
              .asDriverCompleteOnError()
        }
  }
}
