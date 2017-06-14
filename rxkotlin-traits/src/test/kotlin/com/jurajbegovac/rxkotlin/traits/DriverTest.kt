package com.jurajbegovac.rxkotlin.traits

import com.jurajbegovac.rxkotlin.common.TestSchedulerRule
import com.jurajbegovac.rxkotlin.traits.driver.DriverSharingStrategy
import com.jurajbegovac.rxkotlin.traits.driver.asDriver
import com.jurajbegovac.rxkotlin.traits.driver.asDriverCompleteOnError
import com.jurajbegovac.rxkotlin.traits.driver.drive
import com.jurajbegovac.rxkotlin.traits.shared_sequence.*
import common.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rx.Observable
import rx.schedulers.TestScheduler

/** Created by juraj begovac on 06/06/2017. */

class DriverTest {
  var scheduler = TestScheduler()
  var observer = MyTestSubscriber<Int>(scheduler)
  
  @get:Rule
  val testSchedulerRule = TestSchedulerRule()
  
  @Before
  fun setUp() {
    this.scheduler = testSchedulerRule.testScheduler
    this.observer = MyTestSubscriber(scheduler)
  }
  
  @After
  fun tearDown() {
    this.observer.unsubscribe()
  }
  
  fun observableRange(): Observable<Int> =
      Observable
          .range(1, 10, scheduler)
  
  @Test
  fun driverCompleteOnError() {
    this.scheduler.scheduleAt(0) {
      observableRange()
          .map {
            if (it == 5) throw Exception()
            else it
          }
          .asDriverCompleteOnError()
          .drive(observer)
    }
    this.scheduler.advanceTimeBy(10)
    
    assertEquals(listOf(next(0, 1), next(0, 2), next(0, 3), next(0, 4), complete(0)),
                 observer.events())
  }
  
  @Test
  fun driverOnErrorJustReturn() {
    val returnOnError = 7
    
    this.scheduler.scheduleAt(0) {
      observableRange()
          .map {
            if (it == 5) throw Exception()
            else it
          }
          .asDriver(returnOnError)
          .drive(observer)
    }
    this.scheduler.advanceTimeBy(10)
    
    assertEquals(listOf(next(0, 1), next(0, 2), next(0, 3), next(0, 4), next(0, 7), complete(0)),
                 observer.events())
  }
  
  @Test
  fun driverOnErrorDriveWith() {
    this.scheduler.scheduleAt(0) {
      observableRange()
          .map {
            if (it == 5) throw Exception()
            else it
          }
          .asDriver(observableRange().asDriverCompleteOnError())
          .drive(observer)
    }
    this.scheduler.advanceTimeBy(10)
    
    assertEquals(listOf(next(0, 1),
                        next(0, 2),
                        next(0, 3),
                        next(0, 4),
                        next(0, 1),
                        next(0, 2),
                        next(0, 3),
                        next(0, 4),
                        next(0, 5),
                        next(0, 6),
                        next(0, 7),
                        next(0, 8),
                        next(0, 9),
                        next(0, 10),
                        complete(0)),
                 observer.events())
  }
  
  @Test
  fun defer() {
    this.scheduler.scheduleAt(0) {
      DriverSharingStrategy.defer { observableRange().asDriverCompleteOnError() }
          .drive(observer)
    }
    this.scheduler.advanceTimeBy(10)
    
    assertEquals(arrayListOf(next(0, 1),
                             next(0, 2),
                             next(0, 3),
                             next(0, 4),
                             next(0, 5),
                             next(0, 6),
                             next(0, 7),
                             next(0, 8),
                             next(0, 9),
                             next(0, 10),
                             complete(0)), observer.events())
  }
  
  @Test
  fun deferOnErrorComplete() {
    this.scheduler.scheduleAt(0) {
      DriverSharingStrategy.defer {
        if (true) throw Exception()
        else
          observableRange().asDriverCompleteOnError()
      }
          .drive(observer)
    }
    this.scheduler.advanceTimeBy(10)
    
    assertEquals(listOf(complete(0)), observer.events())
  }
  
  @Test
  fun deferOnErrorJustReturn() {
    this.scheduler.scheduleAt(0) {
      DriverSharingStrategy.defer {
        if (true) throw Exception()
        else
          observableRange().asDriverCompleteOnError()
      }
          .drive(observer)
    }
    this.scheduler.advanceTimeBy(10)
    
    assertEquals(listOf(complete(0)), observer.events())
  }
  
  @Test
  fun catchErrorAndCompleteWithoutError() {
    this.scheduler.scheduleAt(0) {
      observableRange()
          .asDriverCompleteOnError()
          .drive(observer)
    }
    this.scheduler.advanceTimeBy(10)
    
    assertEquals(listOf(next(0, 1),
                        next(0, 2),
                        next(0, 3),
                        next(0, 4),
                        next(0, 5),
                        next(0, 6),
                        next(0, 7),
                        next(0, 8),
                        next(0, 9),
                        next(0, 10),
                        complete(0)),
                 observer.events())
  }
  
  @Test
  fun catchErrorAndComplete() {
    this.scheduler.scheduleAt(0) {
      observableRange()
          .map {
            if (it == 5)
              throw Exception()
            else it
          }
          .asDriverCompleteOnError()
          .drive(observer)
    }
    this.scheduler.advanceTimeBy(10)
    assertEquals(listOf(next(0, 1), next(0, 2), next(0, 3), next(0, 4), complete(0)),
                 observer.events())
  }
  
  @Test
  fun catchErrorAndReturn() {
    val returnOnError = 7
    
    observableRange()
        .map {
          if (it == 5)
            throw Exception()
          else it
        }
        .asDriver(returnOnError)
        .drive(observer)
    
    this.scheduler.advanceTimeBy(10)
    
    assertEquals(listOf(next(0, 1), next(0, 2), next(0, 3), next(0, 4), next(0, 7), complete(0)),
                 observer.events())
  }
  
  @Test
  fun map() {
    val returnOnError = 7
    observableRange()
        .asDriverCompleteOnError()
        .map(returnOnError) {
          if (it == 5)
            throw Exception()
          else it
        }
        .drive(observer)
    
    this.scheduler.advanceTimeBy(10)
    
    assertEquals(listOf(next(0, 1),
                        next(0, 2),
                        next(0, 3),
                        next(0, 4),
                        next(0, 7),
                        next(0, 6),
                        next(0, 7),
                        next(0, 8),
                        next(0, 9),
                        next(0, 10),
                        complete(0)), observer.events())
  }
  
  @Test
  fun filter() {
    observableRange()
        .asDriverCompleteOnError()
        .filter {
          if (it == 5)
            throw Exception()
          else true
        }
        .drive(observer)
    
    this.scheduler.advanceTimeBy(10)
    
    assertEquals(listOf(next(0, 1),
                        next(0, 2),
                        next(0, 3),
                        next(0, 4),
                        next(0, 6),
                        next(0, 7),
                        next(0, 8),
                        next(0, 9),
                        next(0, 10),
                        complete(0)), observer.events())
  }
  
  @Test
  fun flatmap() {
    observableRange()
        .asDriverCompleteOnError()
        .flatmap {
          if (it == 5)
            throw Exception()
          else DriverSharingStrategy.just(it)
        }
        .drive(observer)
    
    this.scheduler.advanceTimeBy(10)
    
    assertEquals(listOf(next(0, 1),
                        next(0, 2),
                        next(0, 3),
                        next(0, 4),
                        next(0, 6),
                        next(0, 7),
                        next(0, 8),
                        next(0, 9),
                        next(0, 10),
                        complete(0)), observer.events())
  }
}
