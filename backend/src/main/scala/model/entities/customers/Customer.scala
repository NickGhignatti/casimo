package model.entities.customers

import scala.util.chaining.scalaUtilChainingOps

import model.GlobalConfig
import model.entities.Entity
import model.entities.Movable
import model.entities.customers.CustState.Idle
import model.entities.customers.RiskProfile.Regular
import model.managers.BaseManager
import model.managers.movements.Boids.AlignmentManager
import model.managers.movements.Boids.CohesionManager
import model.managers.movements.Boids.MoverManager
import model.managers.movements.Boids.SeparationManager
import model.managers.movements.Boids.VelocityLimiterManager
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
    maxSpeed: Double = 400.0,
    perceptionRadius: Double = 200,
    avoidRadius: Double = 100.0,
    alignmentWeight: Double = 0,
    cohesionWeight: Double = 0,
    separationWeight: Double = 1
) extends BaseManager[Seq[Customer]]:

  private val boidManager = SeparationManager[Customer](
    avoidRadius = 10
  )
  private val moverManager = MoverManager[Customer]()

  override def update(slice: Seq[Customer])(using
      config: GlobalConfig
  ): Seq[Customer] =
    SeparationManager[Customer](avoidRadius).update(
      slice
    ) pipe CohesionManager().update pipe AlignmentManager().update pipe VelocityLimiterManager(
      maxSpeed
    ).update pipe moverManager.update
