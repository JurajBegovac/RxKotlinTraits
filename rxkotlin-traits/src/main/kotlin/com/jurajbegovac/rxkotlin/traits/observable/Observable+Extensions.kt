package com.jurajbegovac.rxkotlin.traits.observable

import android.util.Log
import io.reactivex.Observable

/** Created by juraj begovac on 06/06/2017. */

fun <Element> Observable<Element>.debug(id: String, logger: (String) -> Unit): Observable<Element> =
    this.doOnNext { logger("$id -> next $it") }
        .doOnError { logger("$id -> error $it") }
        .doOnComplete { logger("$id -> complete") }
        .doOnSubscribe { logger("$id -> subscribe") }
        .doOnDispose { logger("$id -> dispose") }

// todo maybe remove loggers in some other file
fun androidDebugLogger(tag: String): (String) -> Unit = {
  Log.d(tag, it)
}

fun androidInfoLogger(tag: String): (String) -> Unit = {
  Log.i(tag, it)
}

fun consoleLogger(): (String) -> Unit = {
  println(it)
}
