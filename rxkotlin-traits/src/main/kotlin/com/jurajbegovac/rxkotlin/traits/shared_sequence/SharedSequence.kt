package com.jurajbegovac.rxkotlin.traits.shared_sequence

import io.reactivex.Observable
import io.reactivex.Scheduler

/** Created by juraj begovac on 06/06/2017. */

interface SharingStrategyProtocol {
  val scheduler: Scheduler
  fun <Element> share(source: Observable<Element>): Observable<Element>
}

open class SharedSequence<SharingStrategy : SharingStrategyProtocol, Element>(source: Observable<Element>,
                                                                              internal val sharingStrategy: SharingStrategy) {
  companion object {}
  
  internal val source: Observable<Element> = sharingStrategy.share(source)
  
  fun asObservable() = source
}
