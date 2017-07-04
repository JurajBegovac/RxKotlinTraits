package com.jurajbegovac.rxkotlin.traits.shared_sequence

import com.jurajbegovac.rxkotlin.traits.observable.debug

/** Created by juraj begovac on 13/06/2017. */

fun <Element, SharingStrategy : SharingStrategyProtocol> SharedSequence<SharingStrategy, Element>.debug(
    id: String,
    logger: (String) -> Unit): SharedSequence<SharingStrategy, Element> =
    SharedSequence(source.debug(id, logger), sharingStrategy)

fun <Element, SharingStrategy : SharingStrategyProtocol, Result> SharedSequence<SharingStrategy, Element>.map(
    errorValue: Result? = null,
    func: (Element) -> Result): SharedSequence<SharingStrategy, Result> {
  val errorStream = if (errorValue != null) sharingStrategy.just(errorValue) else sharingStrategy.empty()
  return flatMap(errorStream,
                 { element ->
                   try {
                     val result = func(element)
                     sharingStrategy.just(result)
                   } catch (e: Throwable) {
                     reportError(e)
                     errorStream
                   }
                 })
}

fun <Element, SharingStrategy : SharingStrategyProtocol> SharedSequence<SharingStrategy, Element>.filter(
    errorValue: Boolean = false,
    predicate: (Element) -> Boolean): SharedSequence<SharingStrategy, Element> {
  val source = this.source
      .filter {
        try {
          predicate(it)
        } catch (e: Throwable) {
          reportError(e)
          errorValue
        }
      }
  return SharedSequence(source, this.sharingStrategy)
}

fun <Element, SharingStrategy : SharingStrategyProtocol, Result> SharedSequence<SharingStrategy, Element>.flatMap(
    errorValue: SharedSequence<SharingStrategy, Result> = this.sharingStrategy.empty(),
    func: (Element) -> SharedSequence<SharingStrategy, Result>): SharedSequence<SharingStrategy, Result> {
  val source = this.source
      .flatMap {
        try {
          func(it).source
        } catch (e: Throwable) {
          reportError(e)
          errorValue.source
        }
      }
  return SharedSequence(source, this.sharingStrategy)
}

fun <Element, SharingStrategy : SharingStrategyProtocol, Result> SharedSequence<SharingStrategy, Element>.switchMap(
    errorValue: SharedSequence<SharingStrategy, Result> = this.sharingStrategy.empty(),
    func: (Element) -> SharedSequence<SharingStrategy, Result>): SharedSequence<SharingStrategy, Result> {
  val source = this.source
      .switchMap {
        try {
          func(it).source
        } catch (e: Throwable) {
          reportError(e)
          errorValue.source
        }
      }
  return SharedSequence(source, this.sharingStrategy)
}

fun <Element, SharingStrategy : SharingStrategyProtocol, Result> SharedSequence<SharingStrategy, Element>.flatMapIterable(
    errorValue: Iterable<Result> = emptyList<Result>(),
    func: (Element) -> Iterable<Result>): SharedSequence<SharingStrategy, Result> {
  val source = this.source
      .flatMapIterable {
        try {
          func(it)
        } catch (e: Throwable) {
          reportError(e)
          errorValue
        }
      }
  return SharedSequence(source, this.sharingStrategy)
}

fun <Element, SharingStrategy : SharingStrategyProtocol> SharedSequence<SharingStrategy, Element>.distinctUntilChanged(): SharedSequence<SharingStrategy, Element> =
    SharedSequence(this.source.distinctUntilChanged(), this.sharingStrategy)

fun <Element, SharingStrategy : SharingStrategyProtocol> SharedSequence<SharingStrategy, Element>.distinctUntilChanged(
    errorValue: Boolean = false,
    comparator: (Element, Element) -> Boolean): SharedSequence<SharingStrategy, Element> {
  val source = this.source
      .distinctUntilChanged { e1, e2 ->
        try {
          comparator(e1, e2)
        } catch (e: Throwable) {
          reportError(e)
          errorValue
        }
      }
  return SharedSequence(source, this.sharingStrategy)
}

fun <Element, SharingStrategy : SharingStrategyProtocol, Result> SharedSequence<SharingStrategy, Element>.distinctUntilChanged(
    errorValue: Boolean = false,
    keySelector: (Element) -> Result): SharedSequence<SharingStrategy, Element> {
  val source = this.source
      .distinctUntilChanged { e ->
        try {
          keySelector(e)
        } catch (e: Throwable) {
          reportError(e)
          errorValue
        }
      }
  return SharedSequence(source, this.sharingStrategy)
}

fun <Element, SharingStrategy : SharingStrategyProtocol> SharedSequence<SharingStrategy, Element>.startWith(
    item: Element): SharedSequence<SharingStrategy, Element> =
    SharedSequence(this.source.startWith(item), this.sharingStrategy)

fun <Element, SharingStrategy : SharingStrategyProtocol, Result> SharedSequence<SharingStrategy, Element>.scan(
    errorValue: Result,
    initialValue: Result,
    accumulator: (Result, Element) -> Result): SharedSequence<SharingStrategy, Result> {
  val source = this.source
      .scan(initialValue) { r, t ->
        try {
          accumulator(r, t)
        } catch (e: Throwable) {
          reportError(e)
          errorValue
        }
      }
  return SharedSequence(source, this.sharingStrategy)
}

fun <Element, SharingStrategy : SharingStrategyProtocol> SharedSequence<SharingStrategy, Element>.doOnNext(
    onNext: (Element) -> Unit): SharedSequence<SharingStrategy, Element> {
  val source = this.source
      .doOnNext {
        try {
          onNext(it)
        } catch (e: Throwable) {
          reportError(e)
        }
      }
  return SharedSequence(source, this.sharingStrategy)
}
