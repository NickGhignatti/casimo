package model.entities.customers

import model.SimulationState
import model.entities.Entity
import model.entities.Player
import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.customers.RiskProfile.Regular
import model.entities.games.Blackjack
import model.entities.games.GameBuilder
import model.entities.games.GameType
import model.entities.games.Roulette
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
    favouriteGames: Seq[GameType] = Seq(SlotMachine)
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

  override def play: Customer =
    this.changeState(Playing(GameBuilder.blackjack(Vector2D.zero)))

  override def stopPlaying: Customer = this.changeState(Idle)

import scala.util.Random

case class CustomerBuilder(
    id: String = java.util.UUID.randomUUID().toString,
    position: Vector2D = Vector2D.zero,
    direction: Vector2D = Vector2D.zero,
    bankroll: Double = 1000.0,
    riskProfile: RiskProfile = Regular,
    customerState: CustState = Idle,
    betStrategy: BettingStrategy[Customer] = FlatBetting(
      5.0,
      List(1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35)
    ),
    favouriteGames: Seq[GameType] = Seq(Roulette, Blackjack, SlotMachine)
):
  def withId(id: String): CustomerBuilder = copy(id = id)

  def withPosition(pos: Vector2D): CustomerBuilder = copy(position = pos)

  def withDirection(dir: Vector2D): CustomerBuilder = copy(direction = dir)

  def withBankroll(bankroll: Double): CustomerBuilder =
    copy(bankroll = bankroll)

  def withRiskProfile(rp: RiskProfile): CustomerBuilder = copy(riskProfile = rp)

  def withCustomerState(cs: CustState): CustomerBuilder =
    copy(customerState = cs)

  def withBetStrategy(bs: BettingStrategy[Customer]): CustomerBuilder =
    copy(betStrategy = bs)

  def withFavouriteGames(games: Seq[GameType]): CustomerBuilder =
    copy(favouriteGames = games)

  def randomizePosition(
      xRange: (Double, Double),
      yRange: (Double, Double)
  ): CustomerBuilder =
    val x = Random.between(xRange._1, xRange._2)
    val y = Random.between(yRange._1, yRange._2)
    copy(position = Vector2D(x, y))

  def build(): Customer =
    Customer(
      id,
      position,
      direction,
      bankroll,
      riskProfile,
      customerState,
      betStrategy,
      favouriteGames
    )

object CustomerBuilder:
  def apply(): CustomerBuilder = new CustomerBuilder()

  /** Crea un builder con dati random base */
  def random(): CustomerBuilder =
    CustomerBuilder()
      .withId(java.util.UUID.randomUUID().toString)
      .randomizePosition((-100.0, 100.0), (-100.0, 100.0))
      .withBankroll(Random.between(50.0, 500.0))

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
