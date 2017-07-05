package com.jurajbegovac.sample.statemachine

import com.jurajbegovac.rxkotlin.traits.driver.Driver
import com.jurajbegovac.rxkotlin.traits.driver.DriverSharingStrategy
import com.jurajbegovac.rxkotlin.traits.shared_sequence.empty
import com.jurajbegovac.rxkotlin.traits.shared_sequence.flatMap
import com.jurajbegovac.rxkotlin.traits.shared_sequence.just
import com.jurajbegovac.rxkotlin.traits.shared_sequence.scan
import rx.Observable

/** Created by juraj begovac on 04/07/2017. */

fun <Element, State, Emit> Observable<Element>.scanAndMaybeEmit(
    state: State,
    accumulator: (Pair<State, Element>) -> Pair<State, Emit?>): Observable<Emit> {
  return this
      .scan(Pair<State, Emit?>(state, null), { (state, _), element ->
        accumulator(Pair(state, element))
      })
      .flatMap { (_, emit) ->
        emit?.let { Observable.just(it) } ?: Observable.empty()
      }
}

fun <Element, State, Emit> Driver<Element>.scanAndMaybeEmit(
    state: State,
    accumulator: (Pair<State, Element>) -> Pair<State, Emit?>): Driver<Emit> {
  return this
      .scan(errorValue = Pair<State, Emit?>(state, null),
            initialValue = Pair<State, Emit?>(state, null),
            accumulator = { (state, _), element ->
              accumulator(Pair(state, element))
            })
      .flatMap { (_, emit) ->
        emit?.let { DriverSharingStrategy.just(it) } ?: DriverSharingStrategy.empty()
      }
}
