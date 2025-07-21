package model.entities.customers

import model.entities.games.Game

trait CustomerState[T <: CustomerState[T]]:
  val customerState: CustState

  def changeState(newState: CustState): T =
    withCustomerState(newState)

  def withCustomerState(newState: CustState): T

enum CustState:
  case Playing(game: Game)
  case Idle
