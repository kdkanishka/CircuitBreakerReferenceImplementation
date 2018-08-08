package com.kani
case class Cache()

class CircuitBreakerCtx(val currentState: State, fun: Input => Output) {

  //Pre calculate the state before operate

  def operate(): CircuitBreakerCtx = {
    currentState.handle(this, fun)
  }
}

trait State {
  def handle(ctx: CircuitBreakerCtx, fun: Input => Output): CircuitBreakerCtx
}

object Open extends State {
  override def handle(ctx: CircuitBreakerCtx, fun: Input => Output): CircuitBreakerCtx = {
    val timeDiff = System.currentTimeMillis() - DataStore.lastFail < 2000
    if (timeDiff) {
      val newState = Open
      println("Remain open")
      new CircuitBreakerCtx(currentState = newState, fun)
    } else {
      val newState = HalfOpen
      println("New State : HalfOpen")
      new CircuitBreakerCtx(currentState = newState, fun)
    }
  }
}

object Close extends State {
  override def handle(ctx: CircuitBreakerCtx, fun: Input => Output): CircuitBreakerCtx = {
    val result = fun(Input("x"))
    result match {
      case Pass() =>
        val newState = Close
        println("Remain close")
        new CircuitBreakerCtx(currentState = newState, fun)

      case Fail() =>
        DataStore.lastFail = System.currentTimeMillis()
        val newState = Close
        println("Remain close")
        new CircuitBreakerCtx(currentState = newState, fun)

      case Threshold() =>
        val newState = Open
        println("New State : Open")
        new CircuitBreakerCtx(currentState = newState, fun)
    }
  }
}

object HalfOpen extends State {
  override def handle(ctx: CircuitBreakerCtx, fun: Input => Output): CircuitBreakerCtx = {
    val result = fun(Input("x"))
    result match {
      case Fail() =>
        DataStore.lastFail = System.currentTimeMillis()
        val newState = Open
        println("New State : Open")
        new CircuitBreakerCtx(currentState = newState, fun)
      case Pass() =>
        val newState = HalfOpen
        println("Remain HalfOpen")
        new CircuitBreakerCtx(currentState = newState, fun)
      case Threshold() =>
        val newState = Close
        println("New State : p")
        new CircuitBreakerCtx(currentState = newState, fun)
    }
  }
}