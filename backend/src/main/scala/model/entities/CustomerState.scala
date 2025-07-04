package model.entities

trait CustomerState[T <: CustomerState[T]]:
  val customerState: CustState

  def changeState(newState: CustState): T =
    changedState(newState)

  protected def changedState(newState: CustState): T

enum CustState:
  case Playing, Idle, Exited
