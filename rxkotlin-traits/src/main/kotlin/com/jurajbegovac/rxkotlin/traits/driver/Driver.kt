package com.jurajbegovac.rxkotlin.traits.driver

import com.jurajbegovac.rxkotlin.traits.shared_sequence.SharedSequence
import com.jurajbegovac.rxkotlin.traits.shared_sequence.SharingStrategyProtocol
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers

/** Created by juraj begovac on 06/06/2017. */

object DriverSharingStrategy : SharingStrategyProtocol {
  override val scheduler: Scheduler
    get() {
      return AndroidSchedulers.mainThread()
    }
  
  override fun <Element> share(source: Observable<Element>): Observable<Element> =
      source.replay(1).refCount()
}

typealias Driver<Element> = SharedSequence<DriverSharingStrategy, Element>
