package com.jurajbegovac.rxkotlin.traits

import com.jurajbegovac.rxkotlin.common.TestSchedulerRule
import com.jurajbegovac.rxkotlin.common.advanceTimeBy
import com.jurajbegovac.rxkotlin.common.scheduleAt
import com.jurajbegovac.rxkotlin.traits.driver.DriverSharingStrategy
import com.jurajbegovac.rxkotlin.traits.driver.asDriver
import com.jurajbegovac.rxkotlin.traits.driver.asDriverCompleteOnError
import com.jurajbegovac.rxkotlin.traits.driver.drive
import com.jurajbegovac.rxkotlin.traits.shared_sequence.defer
import org.junit.*
import rx.Observable
import rx.observers.TestSubscriber
import rx.schedulers.TestScheduler
import java.util.*

/** Created by juraj begovac on 06/06/2017. */

class DriverTest {
  var scheduler = TestScheduler()
  var observer = TestSubscriber<Int>()
  
  @get:Rule
  val testSchedulerRule = TestSchedulerRule()
  
  @Before
  fun setUp() {
    this.scheduler = testSchedulerRule.testScheduler
    this.observer = TestSubscriber()
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
    
    Assert.assertEquals(arrayListOf(1, 2, 3, 4), observer.onNextEvents)
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
    
    Assert.assertEquals(arrayListOf(1, 2, 3, 4, 7), observer.onNextEvents)
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
    
    Assert.assertEquals(arrayListOf(1, 2, 3, 4, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
                        observer.onNextEvents)
  }
  
  @Test
  fun defer() {
    this.scheduler.scheduleAt(0) {
      DriverSharingStrategy.defer { observableRange().asDriverCompleteOnError() }
          .drive(observer)
    }
    this.scheduler.advanceTimeBy(10)
    
    Assert.assertEquals(arrayListOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), observer.onNextEvents)
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
    
    Assert.assertEquals(ArrayList<Int>(0), observer.onNextEvents)
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
    
    Assert.assertEquals(arrayListOf<Int>(), observer.onNextEvents)
  }
  
  @Test
  fun catchErrorAndCompleteWithoutError() {
    this.scheduler.scheduleAt(0) {
      observableRange()
          .asDriverCompleteOnError()
          .drive(observer)
    }
    this.scheduler.advanceTimeBy(10)
    
    Assert.assertEquals(arrayListOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), observer.onNextEvents)
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
    Assert.assertEquals(arrayListOf(1, 2, 3, 4), observer.onNextEvents)
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
    
    Assert.assertEquals(arrayListOf(1, 2, 3, 4, 7), observer.onNextEvents)
  }
}
