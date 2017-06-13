package com.jurajbegovac.rxkotlin.traits.driver

import com.jurajbegovac.rxkotlin.traits.shared_sequence.empty
import rx.Observable

/** Created by juraj begovac on 06/06/2017. */

fun <Element> Observable<Element>.asDriver(onErrorJustReturn: Element): Driver<Element> {
  val source = this
      .observeOn(DriverSharingStrategy.scheduler)
      .onErrorReturn { onErrorJustReturn }
  return Driver(source, DriverSharingStrategy)
}

fun <Element> Observable<Element>.asDriver(onErrorDriveWith: Driver<Element>): Driver<Element> {
  val source = this
      .observeOn(DriverSharingStrategy.scheduler)
      .onErrorResumeNext(onErrorDriveWith.asObservable())
  return Driver(source, DriverSharingStrategy)
}

fun <Element> Observable<Element>.asDriver(onErrorRecover: (Throwable) -> Driver<Element>): Driver<Element> {
  val source = this
      .observeOn(DriverSharingStrategy.scheduler)
      .onErrorResumeNext { onErrorRecover(it).asObservable() }
  return Driver(source, DriverSharingStrategy)
}

fun <Element> Observable<Element>.asDriverCompleteOnError(): Driver<Element> =
    asDriver(onErrorRecover = { DriverSharingStrategy.empty() })
