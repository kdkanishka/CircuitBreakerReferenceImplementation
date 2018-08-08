package com.kani

case class Input(payload: String)

trait Output

case class Pass() extends Output

case class Fail() extends Output

case class Threshold() extends Output

