package com.jurajbegovac.rxkotlin.traits.driver

import com.jurajbegovac.rxkotlin.traits.shared_sequence.SharedSequence
import rx.Observer
import rx.Subscription

/** Created by juraj begovac on 08/06/2017. */

fun <Element> SharedSequence<DriverSharingStrategy, Element>.drive(onNext: (Element) -> Unit): Subscription =
    this.asObservable().subscribe(onNext)

fun <Element> SharedSequence<DriverSharingStrategy, Element>.drive(observer: Observer<Element>): Subscription =
    this.asObservable().subscribe(observer)

fun <Element> SharedSequence<DriverSharingStrategy, Element>.drive(): Subscription = this.asObservable().subscribe()
