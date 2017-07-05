package com.jurajbegovac.testutils

import rx.Subscriber
import rx.schedulers.TestScheduler

/** Created by juraj on 24/05/2017. */
class MyTestSubscriber<T>(private val scheduler: TestScheduler) : Subscriber<T>() {
  
  private var values: List<Recorded<Event<T>>> = emptyList()
  
  override fun onCompleted() {
    values += Recorded(scheduler.now(), Event.Completed as Event<T>)
  }
  
  override fun onError(e: Throwable?) {
    values += Recorded(scheduler.now(), Event.Error(e ?: Error("Unknown")))
  }
  
  override fun onNext(t: T) {
    values += Recorded(scheduler.now(), Event.Next(t))
  }
  
  fun events() = values
}
