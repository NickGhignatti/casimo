package model.entities.customers

import scala.util.chaining.scalaUtilChainingOps

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
import model.managers.BaseManager
import model.managers.movements.Boids.CohesionManager
import model.managers.movements.Boids.MoverManager
import model.managers.movements.Boids.SeparationManager
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

  def updatedPosition(newPosition: Vector2D): Customer =
    this.copy(position = newPosition)

  protected def updatedBankroll(newRoll: Double): Customer =
    this.copy(bankroll = newRoll)

  protected def changedState(newState: CustState): Customer =
    this.copy(customerState = newState)

  override def updatedDirection(newDirection: Vector2D): Customer =
    this.copy(direction = newDirection)

case class DefaultMovementManager(
    maxSpeed: Double = 5,
    perceptionRadius: Double = 200,
    avoidRadius: Double = 10,
    alignmentWeight: Double = 0,
    cohesionWeight: Double = 0,
    separationWeight: Double = 1
) extends BaseManager[Seq[Customer]]:

  private val boidManager = SeparationManager[Customer](
    perceptionRadius = 200,
    avoidRadius = 10,
    alignmentWeight = 0.5,
    cohesionWeight = 0.5,
    separationWeight = 0.5
  )

  private val moverManager = MoverManager[Customer]()

  override def update(slice: Seq[Customer])(using
      config: GlobalConfig
  ): Seq[Customer] =
    boidManager.update(
      slice
    ) pipe CohesionManager().update pipe moverManager.update
