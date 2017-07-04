package com.jurajbegovac.rxkotlin.traits.shared_sequence

import rx.subjects.PublishSubject
import java.util.concurrent.locks.ReentrantLock

/** Created by juraj begovac on 04/07/2017. */

val lock = ReentrantLock()
val errorSubject: PublishSubject<Throwable> = PublishSubject.create()

fun reportError(e: Throwable) {
  lock.lock()
  try {
    errorSubject.onNext(e)
  } finally {
    lock.unlock()
  }
}
