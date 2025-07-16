package model.entities.customers

import model.SimulationState
import model.entities.Entity
import model.entities.Player
import model.entities.customers.CustState.Idle
import model.entities.customers.RiskProfile.Regular
import model.entities.games.GameType
import model.entities.games.SlotMachine
import model.managers.BaseManager
import model.managers.movements.Boids
import model.managers.movements.Boids._
import model.managers.movements.Context
import model.managers.movements.GamesAttractivenessManager
import model.managers.movements.PlayerSitterManager
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
    favouriteGames: Seq[GameType] = Seq(SlotMachine),
    isPlaying: Boolean = false
) extends Entity,
      Movable[Customer],
      Player[Customer],
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

  override def play: Customer = copy(isPlaying = true)

  override def stopPlaying: Customer = copy(isPlaying = false)

case class DefaultMovementManager(
    maxSpeed: Double = 1000,
    perceptionRadius: Double = 200000,
    avoidRadius: Double = 50,
    alignmentWeight: Double = 1.0,
    cohesionWeight: Double = 1.0,
    separationWeight: Double = 1.0,
    gamesAttractivenessWeight: Double = 1.0,
    sittingRadius: Double = 100
) extends BaseManager[SimulationState]:

  override def update(slice: SimulationState): SimulationState =
    slice
      | GamesAttractivenessAdapter(
        gamesAttractivenessWeight * GamesAttractivenessManager()
          | PlayerSitterManager(sittingRadius)
      )
      | BoidsAdapter(
        PerceptionLimiterManager(perceptionRadius)
          | alignmentWeight * AlignmentManager()
          | cohesionWeight * CohesionManager()
          | separationWeight * SeparationManager(avoidRadius)
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
        .map(c => if !c.player.isPlaying then c | manager else c)
        .map(_.player)
    )

case class BoidsAdapter(manager: BaseManager[Boids.State[Customer]])
    extends BaseManager[SimulationState]:
  override def update(
      slice: SimulationState
  ): SimulationState =
    slice.copy(
      customers = slice.customers
        .map(Boids.State(_, slice.customers))
        .map(c => if !c.boid.isPlaying then c | manager else c)
        .map(_.boid)
    )
