package com.jurajbegovac.sample.statemachine

import com.jurajbegovac.rxkotlin.traits.driver.Driver
import com.jurajbegovac.rxkotlin.traits.driver.DriverSharingStrategy
import com.jurajbegovac.rxkotlin.traits.driver.asDriverCompleteOnError
import com.jurajbegovac.rxkotlin.traits.shared_sequence.*
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.subjects.ReplaySubject

/** Created by juraj on 23/05/2017. */

object Observables {
  fun <S, C> system(
      initState: S,
      accumulator: (S, C) -> S,
      scheduler: Scheduler,
      vararg feedbacks: (Observable<S>) -> Observable<C>): Observable<S> {
    return Observable.defer {
      val replaySubject: ReplaySubject<S> = ReplaySubject.createWithSize(1)
      
      val command = Observable.merge(feedbacks.map { it(replaySubject) })
          .observeOn(scheduler)
      
      command.scan(initState, accumulator)
          .doOnNext { replaySubject.onNext(it) }
    }
  }
}

fun <S, C> SharedSequence.Companion.system(
    initialState: S,
    accumulator: (S, C) -> S,
    vararg feedback: (Driver<S>) -> Driver<C>): SharedSequence<DriverSharingStrategy, S> {
  return DriverSharingStrategy.defer {
    val replaySubject: ReplaySubject<S> = ReplaySubject.createWithSize(1)
    val outputDriver = replaySubject.asDriverCompleteOnError()
    
    val command = DriverSharingStrategy.merge(feedback.map { it(outputDriver) })
    
    command.scan(errorValue = initialState, initialValue = initialState, accumulator = accumulator)
        .doOnNext { replaySubject.onNext(it) }
  }
}
