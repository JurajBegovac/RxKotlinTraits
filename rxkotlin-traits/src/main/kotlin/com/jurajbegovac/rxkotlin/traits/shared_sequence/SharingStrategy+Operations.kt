package com.jurajbegovac.rxkotlin.traits.shared_sequence

import com.jurajbegovac.rxkotlin.traits.observable.debug
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
// todo change this try catch - on error this stream completes
fun <SharingStrategy : SharingStrategyProtocol, Element> SharingStrategy.defer(factory: () -> SharedSequence<SharingStrategy, Element>) =
    SharedSequence(Observable.defer {
      try {
        factory().source
      } catch (e: Exception) {
        Observable.empty<Element>()
      }
    }, this)

fun <SharingStrategy : SharingStrategyProtocol, Element> SharingStrategy.merge(sources: Iterable<SharedSequence<SharingStrategy, out Element>>) =
    SharedSequence(Observable.merge(sources.map { it.source }), this)


fun <Element, Traits : SharingStrategyProtocol> SharedSequence<Traits, Element>.debug(id: String,
                                                                                      logger: (String) -> Unit): SharedSequence<Traits, Element> =
    SharedSequence(this.source.debug(id, logger), this.sharingStrategy)
