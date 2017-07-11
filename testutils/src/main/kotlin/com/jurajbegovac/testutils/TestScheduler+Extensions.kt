package com.jurajbegovac.testutils

import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.merge
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit

/** Created by juraj on 23/05/2017. */

fun <T> TestScheduler.createColdObservable(vararg events: Recorded<Event<T>>): Observable<T> =
    events.map { (delay, value) ->
      Observable.timer(delay, TimeUnit.MILLISECONDS, this)
          .map {
            when (value) {
              is Event.Next -> Notification.createOnNext(value.value)
              is Event.Complete -> Notification.createOnComplete()
              is Event.Error -> Notification.createOnError(value.error)
            }
          }
    }.merge().dematerialize()

fun TestScheduler.scheduleAt(delay: Long, action: () -> Unit): Disposable =
    this.createWorker().schedule(action, delay, TimeUnit.MILLISECONDS)

fun TestScheduler.advanceTimeBy(delay: Long) =
    this.advanceTimeBy(delay, TimeUnit.MILLISECONDS)

fun <T> TestScheduler.createMyTestSubscriber() = MyTestSubscriber<T>(this)
