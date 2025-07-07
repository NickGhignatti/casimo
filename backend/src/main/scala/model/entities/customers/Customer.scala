package model.entities.customers

import CustState.Idle
import model.entities.Entity
import model.entities.Movable
import RiskProfile.Regular
import utils.Vector2D

case class Customer(
    id: String,
    position: Vector2D,
    direction: Vector2D = Vector2D.zero,
    bankroll: Double,
    riskProfile: RiskProfile = Regular,
    customerState: CustState = Idle,
    gameStrategyID: String = "none"
) extends Entity,
      Movable[Customer],
      Bankroll[Customer],
      StatusProfile,
      CustomerState[Customer],
      HasGameStrategy:

  protected def updatedPosition(newPosition: Vector2D): Customer =
    this.copy(position = newPosition)

  protected def updatedBankroll(newRoll: Double): Customer =
    this.copy(bankroll = newRoll)

  protected def changedState(newState: CustState): Customer =
    this.copy(customerState = newState)
