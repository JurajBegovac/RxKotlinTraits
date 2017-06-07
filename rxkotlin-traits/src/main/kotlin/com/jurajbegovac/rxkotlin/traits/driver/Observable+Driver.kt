package com.jurajbegovac.rxkotlin.traits.driver

import com.jurajbegovac.rxkotlin.traits.shared_sequence.SharedSequence
import rx.Observable

/** Created by juraj begovac on 06/06/2017. */

fun <Element> Observable<Element>.asDriver(onErrorJustReturn: Element): Driver<Element> =
    SharedSequence(this.onErrorReturn { onErrorJustReturn }, DriverTraits)

fun <Element> Observable<Element>.asDriver(onErrorDriveWith: (Throwable) -> Driver<Element>): Driver<Element> =
    SharedSequence(this.onErrorResumeNext { onErrorDriveWith(it).source }, DriverTraits)

fun <Element> Observable<Element>.asDriverCompleteOnError(): Driver<Element> =
    SharedSequence(this.onErrorResumeNext { Observable.empty() }, DriverTraits)
