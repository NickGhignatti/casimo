package model.entities.customers

import model.entities.games.Game

trait CustomerState[T <: CustomerState[T]]:
  val customerState: CustState

  def isPlaying: Boolean =
    customerState match
      case CustState.Playing(game) => true
      case CustState.Idle          => false

  def changeState(newState: CustState): T =
    changedState(newState)

  protected def changedState(newState: CustState): T

enum CustState:
  case Playing(game: Game)
  case Idle
