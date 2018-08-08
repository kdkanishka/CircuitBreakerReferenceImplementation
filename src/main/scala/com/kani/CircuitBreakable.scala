package com.kani

trait CircuitBreakable {
  def withCircuitBreaker(currentState: State)(fun: Input => Output): CircuitBreakerCtx = {
    new CircuitBreakerCtx(currentState, fun).operate()
  }
}