package common

import rx.Notification
import rx.Observable
import rx.Subscription
import rx.lang.kotlin.merge
import rx.schedulers.TestScheduler
import java.util.concurrent.TimeUnit

/** Created by juraj on 23/05/2017. */

fun <T> TestScheduler.createColdObservable(vararg events: Recorded<Event<T>>): Observable<T> =
    events.map { (delay, value) ->
      Observable.timer(delay, TimeUnit.MILLISECONDS, this)
          .map {
            when (value) {
              is Event.Next -> Notification.createOnNext(value.value)
              is Event.Completed -> Notification.createOnCompleted()
              is Event.Error -> Notification.createOnError(value.error)
            }
          }
    }.merge().dematerialize()

fun TestScheduler.scheduleAt(delay: Long, action: () -> Unit): Subscription =
    this.createWorker().schedule(action, delay, TimeUnit.MILLISECONDS)

fun TestScheduler.advanceTimeBy(delay: Long) =
    this.advanceTimeBy(delay, TimeUnit.MILLISECONDS)

fun <T> TestScheduler.createMyTestSubscriber() = MyTestSubscriber<T>(this)
