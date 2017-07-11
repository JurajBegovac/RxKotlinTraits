package com.jurajbegovac.testutils

/** Created by juraj on 25/05/2017. */

sealed class Event<T> {
  data class Next<T>(val value: T) : Event<T>()
  data class Error<T>(val error: Throwable) : Event<T>()
  object Complete : Event<Unit>()
}

data class Recorded<out T>(val delay: Long, val value: T)

fun <T> next(delay: Long, value: T) = Recorded(delay, Event.Next(value))
fun <T> error(delay: Long, error: Throwable): Recorded<Event<T>> = Recorded(delay,
                                                                            Event.Error(error))

fun complete(delay: Long): Recorded<Event<Unit>> = Recorded(delay, Event.Complete)
