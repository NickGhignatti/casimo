package model.entities

//TODO: Add a couple (playing, GameID) or a way to know what is being played
trait CustomerState[T <: CustomerState[T]]:
  val customerState: CustState

  def changeState(newState: CustState): T =
    changedState(newState)

  protected def changedState(newState: CustState): T

enum CustState:
  case Playing, Idle, Exited
