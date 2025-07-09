package model.entities.customers

import model.GlobalConfig
import model.entities.Bankroll
import model.entities.CustState
import model.entities.CustState.Idle
import model.entities.CustomerState
import model.entities.Entity
import model.entities.HasGameStrategy
import model.entities.Movable
import model.entities.RiskProfile
import model.entities.RiskProfile.Regular
import model.entities.StatusProfile
import model.entities.games.GameType
import model.managers.BaseManager
import model.managers.movements.Boids
import model.managers.movements.Boids.AlignmentManager
import model.managers.movements.Boids.CohesionManager
import model.managers.movements.Boids.MoverManager
import model.managers.movements.Boids.PerceptionLimiterManager
import model.managers.movements.Boids.SeparationManager
import model.managers.movements.Boids.VelocityLimiterManager
import model.managers.|
import utils.Vector2D

case class Customer(
    id: String,
    position: Vector2D,
    direction: Vector2D = Vector2D.zero,
    bankroll: Double,
    riskProfile: RiskProfile = Regular,
    customerState: CustState = Idle,
    gameStrategyID: String = "none",
    favouriteGames: Seq[GameType] = Seq(GameType.SlotMachine)
) extends Entity,
      Movable[Customer],
      Bankroll[Customer],
      StatusProfile,
      CustomerState[Customer],
      HasGameStrategy:

  def updatedPosition(newPosition: Vector2D): Customer =
    this.copy(position = newPosition)

  protected def updatedBankroll(newRoll: Double): Customer =
    this.copy(bankroll = newRoll)

  protected def changedState(newState: CustState): Customer =
    this.copy(customerState = newState)

  override def updatedDirection(newDirection: Vector2D): Customer =
    this.copy(direction = newDirection)

case class DefaultMovementManager(
    maxSpeed: Double = 50000,
    perceptionRadius: Double = 200000,
    avoidRadius: Double = 10,
    alignmentWeight: Double = 0,
    cohesionWeight: Double = 0,
    separationWeight: Double = 1
) extends BaseManager[Seq[Customer]]:

  override def update(slice: Seq[Customer])(using
      config: GlobalConfig
  ): Seq[Customer] =
    slice | Boids.AdapterManager(
      PerceptionLimiterManager(perceptionRadius)
        | AlignmentManager()
        | CohesionManager()
        | SeparationManager(avoidRadius)
    )
      | VelocityLimiterManager(maxSpeed)
      | MoverManager()
