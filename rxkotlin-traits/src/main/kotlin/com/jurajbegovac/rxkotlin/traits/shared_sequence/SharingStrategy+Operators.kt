package com.jurajbegovac.rxkotlin.traits.shared_sequence

import rx.Observable

/** Created by juraj begovac on 08/06/2017. */

// elementary

fun <SharingStrategy : SharingStrategyProtocol, Element> SharingStrategy.just(element: Element) =
    SharedSequence(Observable.just(element).subscribeOn(this.scheduler), this)

fun <SharingStrategy : SharingStrategyProtocol, Element> SharingStrategy.empty() =
    SharedSequence(Observable.empty<Element>().subscribeOn(this.scheduler), this)

fun <SharingStrategy : SharingStrategyProtocol, Element> SharingStrategy.never() =
    SharedSequence(Observable.never<Element>().subscribeOn(this.scheduler), this)

// operations
fun <SharingStrategy : SharingStrategyProtocol, Element> SharingStrategy.defer(
    errorValue: SharedSequence<SharingStrategy, Element> = empty(),
    factory: () -> SharedSequence<SharingStrategy, Element>): SharedSequence<SharingStrategy, Element> {
  val source = Observable.defer {
    try {
      factory().source
    } catch (e: Exception) {
      errorValue.source
    }
  }
  return SharedSequence(source, this)
}


fun <SharingStrategy : SharingStrategyProtocol, Element> SharingStrategy.merge(vararg sources: SharedSequence<SharingStrategy, Element>) =
    SharedSequence(Observable.merge(sources.map { it.source }), this)

fun <SharingStrategy : SharingStrategyProtocol, Element> SharingStrategy.merge(sources: Iterable<SharedSequence<SharingStrategy, Element>>) =
    SharedSequence(Observable.merge(sources.map { it.source }), this)

fun <SharingStrategy : SharingStrategyProtocol, Element, Result> SharingStrategy.zip(
    errorValue: Result,
    sources: Iterable<SharedSequence<SharingStrategy, out Element>>,
    zipFunction: (Array<Any>) -> Result): SharedSequence<SharingStrategy, Result> {
  val source = Observable.zip(sources.map { it.source }) {
    try {
      zipFunction(it)
    } catch (e: Throwable) {
      errorValue
    }
  }
  return SharedSequence(source, this)
}
