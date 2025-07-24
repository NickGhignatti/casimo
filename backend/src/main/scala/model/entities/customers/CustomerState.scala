package model.entities.customers

import model.entities.customers.CustState.Playing
import model.entities.games.Game

trait CustomerState[T <: CustomerState[T]]:
  val customerState: CustState

  def changeState(newState: CustState): T =
    withCustomerState(newState)

  def withCustomerState(newState: CustState): T

  def getGameOrElse: Option[Game] =
    customerState match
      case Playing(game) => Some(game)
      case _             => Option.empty

  def isPlaying: Boolean = customerState match
    case Playing(_) => true
    case _          => false

enum CustState:
  case Playing(game: Game)
  case Idle
