package model.entities.customers

import model.entities.CustState.Idle
import model.entities.RiskProfile.Regular
import model.entities.{
  Bankroll,
  CustState,
  CustomerState,
  Entity,
  HasGameStrategy,
  Movable,
  RiskProfile,
  StatusProfile
}
import utils.Vector2D

case class Customer(
    id: String,
    position: Vector2D,
    direction: Vector2D = Vector2D.zero,
    bankroll: Double,
    riskProfile: RiskProfile = Regular,
    customerState: CustState = Idle
) extends Entity,
      Movable[Customer],
      Bankroll[Customer],
      StatusProfile,
      CustomerState,
      HasGameStrategy:

  protected def updatedPosition(newPosition: Vector2D): Customer =
    this.copy(position = newPosition)

  protected def updatedBankroll(newRoll: Double): Customer =
    this.copy(bankroll = newRoll)
