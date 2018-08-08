package com.kani

import org.scalatest.FunSuite

class CircuitBreakerCtxTest extends FunSuite with CircuitBreakable {

  test("Consequent successful operations should result in circuit close") {
    var cbCtx = withCircuitBreaker(Close) {
      input => Pass()
    }

    for (i <- 1 to 10) {
      cbCtx = withCircuitBreaker(cbCtx.currentState) {
        input => Pass()
      }
    }

    assert(cbCtx.currentState == Close)
  }

  test("When it is in Close state and reach the Threshold it should change the state from Close to Open") {
    var cbCtx = withCircuitBreaker(Close) {
      input => Fail()
    }

    //Failure state but without reaching the threshold for state transition (close->open)
    for (i <- 1 to 10) {
      cbCtx = withCircuitBreaker(cbCtx.currentState) {
        input => Fail()
      }
    }

    //still it should remain in close state
    assert(cbCtx.currentState == Close)

    cbCtx = withCircuitBreaker(Close) {
      input => Threshold()
    }

    assert(cbCtx.currentState == Open)
  }

  test("It should fail fast when the state become Open") {
    var cbCtx = withCircuitBreaker(Open) {
      input => Fail()
    }

    //the given function will not be executed just fail
    for (i <- 1 to 10) {
      cbCtx = withCircuitBreaker(cbCtx.currentState) {
        input => Pass()
      }
    }

    assert(cbCtx.currentState == Open)
  }

  test("When it is in Open state, state transition from open -> half_open should happen after 2 seconds") {
    var cbCtx = withCircuitBreaker(Open) {
      input => Fail()
    }

    //the given function will not be executed just fail
    for (i <- 1 to 10) {
      cbCtx = withCircuitBreaker(cbCtx.currentState) {
        input => Pass()
      }
    }

    assert(cbCtx.currentState == Open)

    Thread.sleep(3000)

    cbCtx = withCircuitBreaker(cbCtx.currentState) {
      input => Pass()
    }

    assert(cbCtx.currentState == HalfOpen)
  }

  test("When it is in Half Open state it should immediately become Open state when failure happen") {
    var cbCtx = withCircuitBreaker(HalfOpen) {
      input => Fail()
    }

    cbCtx = withCircuitBreaker(cbCtx.currentState) {
      input => Fail()
    }

    assert(cbCtx.currentState == Open)
  }

  test("When it is in half open state it should remain in half open until it reach the success threshold "){
    var cbCtx = withCircuitBreaker(HalfOpen) {
      input => Pass()
    }

    for(i <- 1 to 10){
      cbCtx = withCircuitBreaker(cbCtx.currentState) {
        input => Pass()
      }
    }

    assert(cbCtx.currentState == HalfOpen)
  }

  test("When it is in half open state it should become close state when it reach success threshold"){
    var cbCtx = withCircuitBreaker(HalfOpen) {
      input => Pass()
    }

    assert(cbCtx.currentState == HalfOpen)

    cbCtx = withCircuitBreaker(cbCtx.currentState) {
      input => Threshold()
    }

    assert(cbCtx.currentState == Close)
  }

}
