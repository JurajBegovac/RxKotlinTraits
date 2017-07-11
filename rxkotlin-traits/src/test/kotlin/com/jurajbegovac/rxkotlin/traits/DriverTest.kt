package com.jurajbegovac.rxkotlin.traits

import com.jurajbegovac.rxkotlin.traits.driver.*
import com.jurajbegovac.rxkotlin.traits.shared_sequence.*
import com.jurajbegovac.testutils.*
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rx.Observable
import rx.Subscription
import rx.schedulers.TestScheduler
import rx.subscriptions.Subscriptions

/** Created by juraj begovac on 06/06/2017. */

class DriverTest {
  var scheduler = TestScheduler()
  var observer = scheduler.createMyTestSubscriber<Int>()
  
  @get:Rule
  val testSchedulerRule = TestSchedulerRule()
  
  @Before
  fun setUp() {
    this.scheduler = testSchedulerRule.testScheduler
    this.observer = scheduler.createMyTestSubscriber()
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
        .flatMap {
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
  
  @Test
  fun testDriverSharing_WhenErroring() {
    val observer1 = scheduler.createMyTestSubscriber<Int>()
    val observer2 = scheduler.createMyTestSubscriber<Int>()
    val observer3 = scheduler.createMyTestSubscriber<Int>()
    var subscription1: Subscription = Subscriptions.empty()
    var subscription2: Subscription = Subscriptions.empty()
    var subscription3: Subscription = Subscriptions.empty()
    
    val coldObservable = scheduler.createColdObservable(
        next(10, 0),
        next(20, 1),
        next(30, 2),
        next(40, 3),
        error(50, Error("Test"))
    )
    
    val driver = coldObservable.asDriver(onErrorJustReturn = -1)
    
    scheduler.scheduleAt(200) { subscription1 = driver.asObservable().subscribe(observer1) }
    
    scheduler.scheduleAt(225) { subscription2 = driver.asObservable().subscribe(observer2) }
    
    scheduler.scheduleAt(235) { subscription1.unsubscribe() }
    
    scheduler.scheduleAt(260) { subscription2.unsubscribe() }
    
    // resubscription
    scheduler.scheduleAt(260) { subscription3 = driver.asObservable().subscribe(observer3) }
    
    scheduler.scheduleAt(285) { subscription3.unsubscribe() }
    
    scheduler.advanceTimeBy(1000)
    
    assertEquals(listOf(next(210, 0), next(220, 1), next(230, 2)), observer1.events())
    
    assertEquals(listOf(next(225, 1), next(230, 2), next(240, 3), next(250, -1), complete(250)),
                 observer2.events())
    
    assertEquals(listOf(next(270, 0), next(280, 1)), observer3.events())
  }
  
  @Test
  fun testDriverSharing_WhenCompleted() {
    val observer1 = scheduler.createMyTestSubscriber<Int>()
    val observer2 = scheduler.createMyTestSubscriber<Int>()
    val observer3 = scheduler.createMyTestSubscriber<Int>()
    var subscription1: Subscription = Subscriptions.empty()
    var subscription2: Subscription = Subscriptions.empty()
    var subscription3: Subscription = Subscriptions.empty()
    
    val coldObservable = scheduler.createColdObservable(
        next(10, 0),
        next(20, 1),
        next(30, 2),
        next(40, 3),
        complete(50) as Recorded<Event<Int>>
    )
    
    val driver = coldObservable.asDriver(onErrorJustReturn = -1)
    
    scheduler.scheduleAt(200) { subscription1 = driver.asObservable().subscribe(observer1) }
    
    scheduler.scheduleAt(225) { subscription2 = driver.asObservable().subscribe(observer2) }
    
    scheduler.scheduleAt(235) { subscription1.unsubscribe() }
    
    scheduler.scheduleAt(260) { subscription2.unsubscribe() }
    
    // resubscription
    scheduler.scheduleAt(260) { subscription3 = driver.asObservable().subscribe(observer3) }
    
    scheduler.scheduleAt(285) { subscription3.unsubscribe() }
    
    scheduler.advanceTimeBy(1000)
    
    assertEquals(listOf(next(210, 0), next(220, 1), next(230, 2)), observer1.events())
    
    assertEquals(listOf(next(225, 1), next(230, 2), next(240, 3), complete(250)),
                 observer2.events())
    
    assertEquals(listOf(next(270, 0), next(280, 1)), observer3.events())
  }
  
  @Test
  fun testAsDriver_onErrorJustReturn() {
    val source = scheduler.createColdObservable(next(0, 1), next(0, 2), error(0, Error("Test")))
    val driver = source.asDriver(onErrorJustReturn = -1)
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(1)
    
    assertEquals(listOf(next(0, 1), next(0, 2), next(0, -1), complete(0)), observer.events())
  }
  
  @Test
  fun testAsDriver_onErrorDriveWith() {
    val source = scheduler.createColdObservable(next(0, 1), next(0, 2), error(0, Error("Test")))
    val driver = source.asDriver(onErrorDriveWith = DriverSharingStrategy.just(-1))
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(1)
    
    assertEquals(listOf(next(0, 1), next(0, 2), next(0, -1), complete(0)), observer.events())
  }
  
  @Test
  fun testAsDriver_onErrorRecover() {
    val source = scheduler.createColdObservable(next(0, 1), next(0, 2), error(0, Error("Test")))
    val driver = source.asDriver(onErrorDriveWith = DriverSharingStrategy.empty())
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(1)
    
    assertEquals(listOf(next(0, 1), next(0, 2), complete(0)), observer.events())
  }
  
  @Test
  fun testAsDriver_defer() {
    val source = scheduler.createColdObservable(next(0, 1), next(0, 2), error(0, Error("Test")))
    val driver = DriverSharingStrategy.defer { source.asDriver(onErrorJustReturn = -1) }
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(1)
    
    assertEquals(listOf(next(0, 1), next(0, 2), next(0, -1), complete(0)), observer.events())
  }
  
  @Test
  fun testAsDriver_map() {
    val source = scheduler.createColdObservable(next(0, 1), next(0, 2), error(0, Error("Test")))
    val driver = source.asDriver(onErrorJustReturn = -1).map(-2) { it + 1 }
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(1)
    
    assertEquals(listOf(next(0, 2), next(0, 3), next(0, 0), complete(0)), observer.events())
  }
  
  @Test
  fun testAsDriver_filter() {
    val source = scheduler.createColdObservable(next(0, 1), next(0, 2), error(0, Error("Test")))
    val driver = source.asDriver(onErrorJustReturn = -1).filter { it % 2 == 0 }
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(1)
    
    assertEquals(listOf(next(0, 2), complete(0)), observer.events())
  }
  
  @Test
  fun testAsDriver_switchMap() {
    val observable = scheduler.createColdObservable(next(0, 0),
                                                    next(1, 1),
                                                    error<Int>(2, Error("Test")),
                                                    complete(3) as Recorded<Event<Int>>)
    val observable1 = scheduler.createColdObservable(next(0, 1),
                                                     next(0, 2),
                                                     error(0, Error("Test")))
    val observable2 = scheduler.createColdObservable(next(0, 10),
                                                     next(0, 11),
                                                     error(0, Error("Test")))
    val errorObservable = scheduler.createColdObservable(complete(0) as Recorded<Event<Int>>)
    
    val drivers = arrayListOf(
        observable1.asDriver(onErrorJustReturn = -2),
        observable2.asDriver(onErrorJustReturn = -3),
        errorObservable.asDriver(onErrorJustReturn = -4)
    )
    
    val driver = observable.asDriver(onErrorJustReturn = 2).switchMap { drivers[it] }
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(10)
    
    assertEquals(listOf(next(0, 1),
                        next(0, 2),
                        next(0, -2),
                        next(1, 10),
                        next(1, 11),
                        next(1, -3),
                        complete(2)),
                 observer.events())
  }
  
  @Test
  fun testAsDriver_switchMap_overlapping() {
    val observable = scheduler.createColdObservable(next(0, 0),
                                                    next(1, 1),
                                                    error<Int>(2, Error("Test")),
                                                    complete(3) as Recorded<Event<Int>>)
    val observable1 = scheduler.createColdObservable(next(0, 1),
                                                     error(0, Error("Test")),
                                                     next(1, 2))
    val observable2 = scheduler.createColdObservable(next(0, 10),
                                                     error(0, Error("Test")),
                                                     next(1, 11))
    val errorObservable = scheduler.createColdObservable(complete(0) as Recorded<Event<Int>>)
    
    val drivers = arrayListOf(
        observable1.asDriver(onErrorJustReturn = -2),
        observable2.asDriver(onErrorJustReturn = -3),
        errorObservable.asDriver(onErrorJustReturn = -4)
    )
    
    val driver = observable.asDriver(onErrorJustReturn = 2).switchMap { drivers[it] }
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(10)
    
    assertEquals(listOf(next(0, 1),
                        next(0, -2),
                        next(1, 10),
                        next(1, -3),
                        complete(2)),
                 observer.events())
  }
  
  @Test
  fun testAsDriver_doOnNext() {
    val observable = scheduler.createColdObservable(next(0, 1),
                                                    next(0, 2),
                                                    error<Int>(0, Error("Test")))
    
    var events = emptyArray<Int>()
    
    val driver = observable.asDriver(onErrorJustReturn = -1).doOnNext {
      events += it
    }
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(1)
    
    assertEquals(listOf(next(0, 1), next(0, 2), next(0, -1), complete(0)),
                 observer.events())
    val expectedEvents = arrayOf(1, 2, -1)
    assertArrayEquals(expectedEvents, events)
  }
  
  @Test
  fun testAsDriver_distinctUntilChanged1() {
    val observable = scheduler.createColdObservable(next(0, 1),
                                                    next(0, 2),
                                                    next(0, 2),
                                                    error<Int>(0, Error("Test")))
    val driver = observable.asDriver(onErrorJustReturn = -1).distinctUntilChanged()
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(1)
    
    assertEquals(listOf(next(0, 1), next(0, 2), next(0, -1), complete(0)),
                 observer.events())
  }
  
  @Test
  fun testAsDriver_distinctUntilChanged2() {
    val observable = scheduler.createColdObservable(next(0, 1),
                                                    next(0, 2),
                                                    next(0, 2),
                                                    error<Int>(0, Error("Test")))
    val driver = observable.asDriver(onErrorJustReturn = -1).distinctUntilChanged { e -> e }
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(1)
    
    assertEquals(listOf(next(0, 1), next(0, 2), next(0, -1), complete(0)),
                 observer.events())
  }
  
  @Test
  fun testAsDriver_distinctUntilChanged3() {
    val observable = scheduler.createColdObservable(next(0, 1),
                                                    next(0, 2),
                                                    next(0, 2),
                                                    error<Int>(0, Error("Test")))
    val driver = observable.asDriver(onErrorJustReturn = -1).distinctUntilChanged { e1, e2 -> e1 == e2 }
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(1)
    
    assertEquals(listOf(next(0, 1), next(0, 2), next(0, -1), complete(0)),
                 observer.events())
  }
  
  @Test
  fun testAsDriver_flatMap() {
    val observable = scheduler.createColdObservable(next(0, 1),
                                                    next(0, 2),
                                                    error<Int>(0, Error("Test")))
    val driver = observable.asDriver(onErrorJustReturn = -1).flatMap {
      DriverSharingStrategy.just(it + 1)
    }
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(1)
    
    assertEquals(listOf(next(0, 2), next(0, 3), next(0, 0), complete(0)),
                 observer.events())
  }
  
  @Test
  fun testAsDriver_mergeSync() {
    val factories: Array<(Driver<Int>) -> Driver<Int>> = arrayOf(
        { driver -> DriverSharingStrategy.merge(driver) },
        { driver -> DriverSharingStrategy.merge(*arrayOf(driver)) },
        { driver -> DriverSharingStrategy.merge(arrayListOf(driver)) }
    )
    
    val observers = ArrayList<MyTestSubscriber<Int>>(factories.size)
    
    factories.forEach {
      val observable = scheduler.createColdObservable(next(0, 1),
                                                      next(0, 2),
                                                      error<Int>(0, Error("Test")))
      val driver = it(observable.asDriver(onErrorJustReturn = -1))
      
      val observer = scheduler.createMyTestSubscriber<Int>()
      driver.drive(observer)
      
      observers.add(observer)
    }
    
    scheduler.advanceTimeBy(1)
    
    observers.forEach {
      assertEquals(listOf(next(0, 1), next(0, 2), next(0, -1), complete(0)),
                   it.events())
    }
  }
  
  @Test
  fun testAsDriver_merge() {
    val observable: Observable<Int> = scheduler.createColdObservable(next(0, 1),
                                                                     next(0, 2),
                                                                     error<Int>(0, Error("Test")))
    
    val driver = DriverSharingStrategy
        .merge(observable.asDriver(onErrorJustReturn = -1).flatMap { DriverSharingStrategy.just(it + 1) })
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(1)
    
    assertEquals(listOf(next(0, 2), next(0, 3), next(0, 0), complete(0)),
                 observer.events())
  }
  
  @Test
  fun testAsDriver_scan() {
    val observable: Observable<Int> = scheduler.createColdObservable(next(0, 1),
                                                                     next(0, 2),
                                                                     error<Int>(0, Error("Test")))
    
    val driver = observable.asDriver(onErrorJustReturn = -1).scan(-2, 0) { a, n -> a + n }
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(1)
    
    assertEquals(listOf(next(0, 0), next(0, 1), next(0, 3), next(0, 2), complete(0)),
                 observer.events())
  }
  
  @Test
  fun testAsDriver_startWith() {
    val observable: Observable<Int> = scheduler.createColdObservable(next(0, 1),
                                                                     next(0, 2),
                                                                     error<Int>(0, Error("Test")))
    
    val driver = observable.asDriver(onErrorJustReturn = -1).startWith(0)
    
    driver.drive(observer)
    
    scheduler.advanceTimeBy(1)
    
    assertEquals(listOf(next(0, 0), next(0, 1), next(0, 2), next(0, -1), complete(0)),
                 observer.events())
  }
}
