package model.entities.customers

import model.SimulationState
import model.entities.Entity
import model.entities.GamesAttracted
import model.entities.customers.CustState.Idle
import model.entities.customers.RiskProfile.Regular
import model.entities.games.GameType
import model.entities.games.SlotMachine
import model.managers.BaseManager
import model.managers.movements.Boids
import model.managers.movements.Boids._
import model.managers.movements.Context
import model.managers.movements.GamesAttractivenessManager
import model.managers.|
import utils.Vector2D

case class Customer(
    id: String,
    position: Vector2D,
    direction: Vector2D = Vector2D.zero,
    bankroll: Double,
    riskProfile: RiskProfile = Regular,
    customerState: CustState = Idle,
    betStrategy: BettingStrategy[Customer] = FlatBetting(5.0, 1),
    favouriteGames: Seq[GameType] = Seq(SlotMachine)
) extends Entity,
      Movable[Customer],
      GamesAttracted[Customer],
      Bankroll[Customer],
      StatusProfile,
      CustomerState[Customer],
      HasBetStrategy[Customer]:

  def updatedPosition(newPosition: Vector2D): Customer =
    this.copy(position = newPosition)

  protected def updatedBankroll(newRoll: Double): Customer =
    this.copy(bankroll = newRoll)

  protected def changedState(newState: CustState): Customer =
    this.copy(customerState = newState)

  override def updatedDirection(newDirection: Vector2D): Customer =
    this.copy(direction = newDirection)

  protected def changedBetStrategy(
      newStrat: BettingStrategy[Customer]
  ): Customer =
    this.copy(betStrategy = newStrat)

case class DefaultMovementManager(
    maxSpeed: Double = 1000,
    perceptionRadius: Double = 200000,
    avoidRadius: Double = 50
) extends BaseManager[SimulationState]:

  override def update(slice: SimulationState): SimulationState =
    slice
      | GamesAttractivenessAdapter(GamesAttractivenessManager())
      | BoidsAdapter(
        PerceptionLimiterManager(perceptionRadius)
          | AlignmentManager()
          | CohesionManager()
          | SeparationManager(avoidRadius)
          | VelocityLimiterManager(maxSpeed)
          | MoverManager()
      )

case class GamesAttractivenessAdapter(manager: BaseManager[Context[Customer]])
    extends BaseManager[SimulationState]:
  override def update(
      slice: SimulationState
  ): SimulationState =
    slice.copy(
      customers = slice.customers
        .map(Context(_, slice.games))
        .map(_ | manager)
        .map(_.customer)
    )

case class BoidsAdapter(manager: BaseManager[Boids.State[Customer]])
    extends BaseManager[SimulationState]:
  override def update(
      slice: SimulationState
  ): SimulationState =
    slice.copy(
      customers = slice.customers
        .map(Boids.State(_, slice.customers))
        .map(_ | manager)
        .map(_.boid)
    )
