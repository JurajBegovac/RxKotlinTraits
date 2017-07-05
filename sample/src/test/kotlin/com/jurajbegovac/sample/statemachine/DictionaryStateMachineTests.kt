package com.jurajbegovac.sample.statemachine

import com.jurajbegovac.rxkotlin.traits.driver.drive
import com.jurajbegovac.testutils.TestSchedulerRule
import com.jurajbegovac.testutils.advanceTimeBy
import com.jurajbegovac.testutils.scheduleAt
import org.junit.*
import rx.observers.TestSubscriber
import rx.schedulers.TestScheduler

/** Created by juraj on 23/05/2017. */

class DictionaryStateMachineTests {
  var scheduler = TestScheduler()
  var observer = TestSubscriber<Map<Int, TestState>>()
  var stateMachine: DictionaryStateMachine<Int, TestState>? = null
  
  @get:Rule
  val testSchedulerRule = TestSchedulerRule()
  
  @Before
  fun setUp() {
    this.scheduler = testSchedulerRule.testScheduler
    this.observer = TestSubscriber()
    this.stateMachine = DictionaryStateMachine(TestStateFeedbackLoops(this.scheduler)::feedbackLoops)
    this.stateMachine!!.state.drive(this.observer)
  }
  
  @After
  fun tearDown() {
    this.observer.unsubscribe()
    this.stateMachine!!.dispose()
  }
  
  fun performOp1(key: Int) {
    this.stateMachine!!.transition(Pair(key, TestState.Operation1(TestState.Operation.Start)))
  }
  
  fun performOp2(key: Int) {
    this.stateMachine!!.transition(Pair(key, TestState.Operation2(TestState.Operation.Start)))
  }
  
  @Test
  fun singleKeySingleOperation() {
    this.scheduler.scheduleAt(300) { this.performOp1(1) }
    this.scheduler.advanceTimeBy(1000)
    
    Assert.assertEquals(listOf(
        mapOf(),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Finish))),
        mapOf()
    ), this.observer.onNextEvents)
  }
  
  @Test
  fun singleKeyMultipleSameSequentialOperations() {
    this.scheduler.scheduleAt(300) { this.performOp1(1) }
    this.scheduler.scheduleAt(400) { this.performOp1(1) }
    this.scheduler.scheduleAt(500) { this.performOp1(1) }
    
    this.scheduler.advanceTimeBy(1000)
    
    Assert.assertEquals(listOf(
        mapOf(),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Finish))),
        mapOf(),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Finish))),
        mapOf(),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Finish))),
        mapOf()
    ), this.observer.onNextEvents)
  }
  
  @Test
  fun singleKeyMultipleDifferentSequentialOperations() {
    this.scheduler.scheduleAt(300) { this.performOp2(1) }
    this.scheduler.scheduleAt(400) { this.performOp2(1) }
    this.scheduler.scheduleAt(500) { this.performOp1(1) }
    this.scheduler.scheduleAt(600) { this.performOp2(1) }
    this.scheduler.scheduleAt(700) { this.performOp1(1) }
    
    this.scheduler.advanceTimeBy(1000)
    
    Assert.assertEquals(listOf(
        mapOf(),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Work("op2")))),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Finish))),
        mapOf(),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Work("op2")))),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Finish))),
        mapOf(),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Finish))),
        mapOf(),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Work("op2")))),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Finish))),
        mapOf(),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Finish))),
        mapOf()
    ), this.observer.onNextEvents)
  }
  
  @Test
  fun singleKeyInterruptOperationWithSameOperation() {
    this.scheduler.scheduleAt(300) { this.performOp1(1) }
    this.scheduler.scheduleAt(320) { this.performOp1(1) }
    this.scheduler.scheduleAt(340) { this.performOp1(1) }
    
    this.scheduler.advanceTimeBy(1000)
    
    Assert.assertEquals(listOf(
        mapOf(),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Finish))),
        mapOf()
    ), this.observer.onNextEvents)
  }
  
  @Test
  fun singleKeyInterruptOperationWithDifferentOperation() {
    this.scheduler.scheduleAt(300) { this.performOp1(1) }
    this.scheduler.scheduleAt(320) { this.performOp2(1) }
    
    this.scheduler.advanceTimeBy(1000)
    
    Assert.assertEquals(listOf(
        mapOf(),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Work("op2")))),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Finish))),
        mapOf()
    ), this.observer.onNextEvents)
  }
  
  @Test
  fun twoKeysSameOperationSequential() {
    this.scheduler.scheduleAt(300) { this.performOp1(1) }
    this.scheduler.scheduleAt(400) { this.performOp1(2) }
    
    this.scheduler.advanceTimeBy(1000)
    
    Assert.assertEquals(listOf(
        mapOf(),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Finish))),
        mapOf(),
        mapOf(Pair(2, TestState.Operation1(TestState.Operation.Start))),
        mapOf(Pair(2, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(Pair(2, TestState.Operation1(TestState.Operation.Finish))),
        mapOf()
    ), this.observer.onNextEvents)
  }
  
  @Test
  fun twoKeysDifferentOperationSequential() {
    this.scheduler.scheduleAt(300) { this.performOp1(1) }
    this.scheduler.scheduleAt(400) { this.performOp2(2) }
    
    this.scheduler.advanceTimeBy(1000)
    
    Assert.assertEquals(listOf(
        mapOf(),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Finish))),
        mapOf(),
        mapOf(Pair(2, TestState.Operation2(TestState.Operation.Start))),
        mapOf(Pair(2, TestState.Operation2(TestState.Operation.Work("op2")))),
        mapOf(Pair(2, TestState.Operation2(TestState.Operation.Finish))),
        mapOf()
    ), this.observer.onNextEvents)
  }
  
  @Test
  fun twoKeysSameOperationOverlapping() {
    this.scheduler.scheduleAt(300) { this.performOp1(1) }
    this.scheduler.scheduleAt(310) { this.performOp1(2) }
    
    this.scheduler.advanceTimeBy(1000)
    
    Assert.assertEquals(listOf(
        mapOf(),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(
            Pair(1, TestState.Operation1(TestState.Operation.Work("op1"))),
            Pair(2, TestState.Operation1(TestState.Operation.Start))
        ),
        mapOf(
            Pair(1, TestState.Operation1(TestState.Operation.Work("op1"))),
            Pair(2, TestState.Operation1(TestState.Operation.Work("op1")))
        ),
        mapOf(
            Pair(1, TestState.Operation1(TestState.Operation.Finish)),
            Pair(2, TestState.Operation1(TestState.Operation.Work("op1")))
        ),
        mapOf(Pair(2, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(Pair(2, TestState.Operation1(TestState.Operation.Finish))),
        mapOf()
    ), this.observer.onNextEvents)
  }
  
  @Test
  fun twoKeysDifferentOperationOverlapping() {
    this.scheduler.scheduleAt(300) { this.performOp2(1) }
    this.scheduler.scheduleAt(310) { this.performOp1(2) }
    
    this.scheduler.advanceTimeBy(1000)
    
    Assert.assertEquals(listOf(
        mapOf(),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Work("op2")))),
        mapOf(
            Pair(1, TestState.Operation2(TestState.Operation.Work("op2"))),
            Pair(2, TestState.Operation1(TestState.Operation.Start))
        ),
        mapOf(
            Pair(1, TestState.Operation2(TestState.Operation.Work("op2"))),
            Pair(2, TestState.Operation1(TestState.Operation.Work("op1")))
        ),
        mapOf(
            Pair(1, TestState.Operation2(TestState.Operation.Finish)),
            Pair(2, TestState.Operation1(TestState.Operation.Work("op1")))
        ),
        mapOf(Pair(2, TestState.Operation1(TestState.Operation.Work("op1")))),
        mapOf(Pair(2, TestState.Operation1(TestState.Operation.Finish))),
        mapOf()
    ), this.observer.onNextEvents)
  }
  
  @Test
  fun twoKeysDifferentOperationOverlappingWithInterrupts() {
    this.scheduler.scheduleAt(300) { this.performOp2(1) }
    this.scheduler.scheduleAt(310) { this.performOp1(2) }
    this.scheduler.scheduleAt(313) { this.performOp1(1) }
    this.scheduler.scheduleAt(337) { this.performOp2(2) }
    
    this.scheduler.advanceTimeBy(1000)
    
    Assert.assertEquals(listOf(
        mapOf(),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Start))),
        mapOf(Pair(1, TestState.Operation2(TestState.Operation.Work("op2")))),
        mapOf(
            Pair(1, TestState.Operation2(TestState.Operation.Work("op2"))),
            Pair(2, TestState.Operation1(TestState.Operation.Start))
        ),
        mapOf(
            Pair(1, TestState.Operation1(TestState.Operation.Start)),
            Pair(2, TestState.Operation1(TestState.Operation.Start))
        ),
        mapOf(
            Pair(1, TestState.Operation1(TestState.Operation.Start)),
            Pair(2, TestState.Operation1(TestState.Operation.Work("op1")))
        ),
        mapOf(
            Pair(1, TestState.Operation1(TestState.Operation.Work("op1"))),
            Pair(2, TestState.Operation1(TestState.Operation.Work("op1")))
        ),
        mapOf(
            Pair(1, TestState.Operation1(TestState.Operation.Work("op1"))),
            Pair(2, TestState.Operation1(TestState.Operation.Finish))
        ),
        mapOf(
            Pair(1, TestState.Operation1(TestState.Operation.Work("op1"))),
            Pair(2, TestState.Operation2(TestState.Operation.Start))
        ),
        mapOf(
            Pair(1, TestState.Operation1(TestState.Operation.Finish)),
            Pair(2, TestState.Operation2(TestState.Operation.Start))
        ),
        mapOf(
            Pair(1, TestState.Operation1(TestState.Operation.Finish)),
            Pair(2, TestState.Operation2(TestState.Operation.Work("op2")))
        ),
        mapOf(Pair(2, TestState.Operation2(TestState.Operation.Work("op2")))),
        mapOf(Pair(2, TestState.Operation2(TestState.Operation.Finish))),
        mapOf()
    ), this.observer.onNextEvents)
  }
}
