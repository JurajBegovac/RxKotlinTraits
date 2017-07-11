package com.jurajbegovac.rxkotlin.traits.driver

import com.jurajbegovac.rxkotlin.traits.shared_sequence.SharedSequence
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/** Created by juraj begovac on 08/06/2017. */

fun <Element> SharedSequence<DriverSharingStrategy, Element>.drive(onNext: (Element) -> Unit): Disposable =
    this.asObservable().subscribe(onNext)

fun <Element> SharedSequence<DriverSharingStrategy, Element>.drive(observer: Observer<Element>): Disposable =
    this.asObservable().subscribe({ observer.onNext(it) },
                                  { observer.onError(it) },
                                  { observer.onComplete() },
                                  { observer.onSubscribe(it) })

fun <Element> SharedSequence<DriverSharingStrategy, Element>.drive(): Disposable = this.asObservable().subscribe()
