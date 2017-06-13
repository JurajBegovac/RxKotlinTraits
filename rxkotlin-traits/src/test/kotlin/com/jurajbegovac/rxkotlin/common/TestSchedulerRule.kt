package com.jurajbegovac.rxkotlin.common

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import rx.Scheduler
import rx.android.plugins.RxAndroidPlugins
import rx.android.plugins.RxAndroidSchedulersHook
import rx.internal.schedulers.ExecutorScheduler
import rx.plugins.RxJavaHooks
import rx.schedulers.TestScheduler

/** Created by juraj begovac on 09/06/2017. */

class TestSchedulerRule : TestRule {
  
  private val immediate = object : Scheduler() {
    override fun createWorker(): Worker = ExecutorScheduler(Runnable::run).createWorker()
  }
  
  val testScheduler = TestScheduler()
  
  override fun apply(base: Statement, d: Description): Statement {
    return object : Statement() {
      @Throws(Throwable::class)
      override fun evaluate() {
        RxJavaHooks.reset()
        RxJavaHooks.setOnIOScheduler { testScheduler }
        RxJavaHooks.setOnComputationScheduler { testScheduler }
        RxJavaHooks.setOnNewThreadScheduler { testScheduler }
        RxAndroidPlugins.getInstance().reset()
        RxAndroidPlugins.getInstance().registerSchedulersHook(object : RxAndroidSchedulersHook() {
          override fun getMainThreadScheduler(): Scheduler = immediate
        })
        
        base.evaluate()
      }
    }
  }
}
