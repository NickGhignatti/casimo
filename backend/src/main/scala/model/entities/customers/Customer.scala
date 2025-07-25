package model.entities.customers

import scala.util.Random

import model.SimulationState
import model.entities.Entity
import model.entities.Player
import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.customers.RiskProfile.Regular
import model.entities.games.Game
import model.entities.games.GameType
import model.entities.games.Roulette
import model.managers.BaseManager
import model.managers.movements.AvoidObstaclesManager
import model.managers.movements.Boids
import model.managers.movements.Boids._
import model.managers.movements.PlayerManagers
import model.managers.movements.PlayerManagers.GamesAttractivenessManager
import model.managers.movements.PlayerManagers.PlayerSitterManager
import model.managers.|
import utils.Vector2D

/** The `Customer` is an entity which moves around the casino and plays games.
  */
case class Customer(
    id: String = java.util.UUID.randomUUID().toString,
    position: Vector2D = Vector2D.zero,
    direction: Vector2D = Vector2D.zero,
    bankroll: Double = 1000.0,
    startingBankroll: Double = 1000.0,
    boredom: Double = 15.0,
    frustration: Double = 0.0,
    riskProfile: RiskProfile = Regular,
    customerState: CustState = Idle,
    betStrategy: BettingStrategy[Customer] = FlatBetting(10.0, defaultRedBet),
    favouriteGame: GameType = Roulette,
    previousPosition: Option[Vector2D] = Option.empty
) extends Entity,
      MovableWithPrevious[Customer],
      Bankroll[Customer],
      BoredomFrustration[Customer],
      StatusProfile,
      CustomerState[Customer],
      HasBetStrategy[Customer],
      Player[Customer]:

  def withId(newId: String): Customer =
    this.copy(id = newId)

  def withPosition(newPosition: Vector2D): Customer =
    this.copy(position = newPosition, previousPosition = Some(position))

  def withBankroll(newRoll: Double, update: Boolean = false): Customer =
    if update then this.copy(bankroll = newRoll)
    else this.copy(bankroll = newRoll, startingBankroll = newRoll)

  def withBoredom(newBoredom: Double): Customer =
    this.copy(boredom = newBoredom)

  def withFrustration(newFrustration: Double): Customer =
    this.copy(frustration = newFrustration)

  def withCustomerState(newState: CustState): Customer =
    this.copy(customerState = newState)

  def withDirection(newDirection: Vector2D): Customer =
    this.copy(direction = newDirection)

  def withBetStrategy(
      newStrat: BettingStrategy[Customer]
  ): Customer =
    this.copy(betStrategy = newStrat)

  def withFavouriteGames(newFavGame: GameType): Customer =
    this.copy(favouriteGame = newFavGame)

  def withProfile(profile: RiskProfile): Customer =
    this.copy(riskProfile = profile)

  def randomizePosition(
      xRange: (Double, Double),
      yRange: (Double, Double)
  ): Customer =
    val x = Random.between(xRange._1, xRange._2)
    val y = Random.between(yRange._1, yRange._2)
    withPosition(Vector2D(x, y))

  def random(): Customer =
    Customer()
      .withId(java.util.UUID.randomUUID().toString)
      .randomizePosition((-100.0, 100.0), (-100.0, 100.0))
      .withBankroll(Random.between(50.0, 500.0))

  override def play(game: Game): Customer = withCustomerState(Playing(game))

  override def stopPlaying: Customer = withCustomerState(Idle)

  override def isPlaying: Boolean = customerState match
    case Playing(_) => true
    case _          => false

/** This manager implements the default behaviour for the customer. It combines
  * the boid-like behaviours, the games' attractiveness and avoids the
  * collisions of customers with walls and games
  */
case class DefaultMovementManager(
    maxSpeed: Double = 1000,
    perceptionRadius: Double = 200000,
    avoidRadius: Double = 50,
    alignmentWeight: Double = 1.0,
    cohesionWeight: Double = 1.0,
    separationWeight: Double = 1.0,
    gamesAttractivenessWeight: Double = 1.0,
    sittingRadius: Double = 100,
    boredomIncrease: Double = 0.1
) extends BaseManager[SimulationState]:

  override def update(slice: SimulationState): SimulationState =
    slice | FilterManager(
      GamesAttractivenessAdapter(
        gamesAttractivenessWeight * GamesAttractivenessManager(boredomIncrease)
          | PlayerSitterManager(sittingRadius)
      )
        | BoidsAdapter(
          PerceptionLimiterManager(perceptionRadius)
            | alignmentWeight * AlignmentManager()
            | cohesionWeight * CohesionManager()
            | separationWeight * SeparationManager(avoidRadius)
            | VelocityLimiterManager(maxSpeed)
        )
    )
      | WallAvoidingAdapter(AvoidObstaclesManager())
      | BoidsAdapter(MoverManager())

/** This manager adapts the `manager` which updates players contexts to one
  * which manipulates `SimulationState`. The contexts are updated one-by-one,
  * that is the changes done by the previous update are seen by the next
  * updates.
  * @param manager
  *   the adapted manager
  */
private case class GamesAttractivenessAdapter(
    manager: BaseManager[PlayerManagers.Context[Customer]]
) extends BaseManager[SimulationState]:
  override def update(
      slice: SimulationState
  ): SimulationState =
    import PlayerManagers.Context
    slice.customers.foldLeft(slice)((state, customer) =>
      val updatedContext = Context(customer, state.games) | manager
      state.copy(
        customers = state.customers.map(c =>
          if c.id == updatedContext.player.id then updatedContext.player else c
        ),
        games = state.games.map(game =>
          updatedContext.games.find(_.id == game.id).get
        )
      )
    )

/** This manager adapts the `manager` which updates boids contexts to one which
  * manipulates `SimulationState`
  * @param manager
  *   the adapted manager
  */
private case class BoidsAdapter(manager: BaseManager[Boids.Context[Customer]])
    extends BaseManager[SimulationState]:
  override def update(
      slice: SimulationState
  ): SimulationState =
    slice.copy(
      customers = slice.customers
        .map(Boids.Context(_, slice.customers))
        .map(_ | manager)
        .map(_.boid)
    )

/** This manager adapts the `manager` which updates avoid obstacles contexts to
  * one which manipulates `SimulationState`
  *
  * @param manager
  *   the adapted manager
  */
private case class WallAvoidingAdapter(
    manager: BaseManager[AvoidObstaclesManager.Context[Customer]]
) extends BaseManager[SimulationState]:
  override def update(
      slice: SimulationState
  ): SimulationState =
    slice.copy(
      customers = slice.customers
        .map(c => AvoidObstaclesManager.Context(c, slice.walls ++ slice.games))
        .map(_ | manager)
        .map(_.movable)
    )

/** This manager filters out customers which are currently playing and games
  * fully occupied and updates only the remaining customers
  *
  * @param manager
  *   the manager used to update the filtered `SimulationState`
  */
private case class FilterManager(manager: BaseManager[SimulationState])
    extends BaseManager[SimulationState]:
  override def update(slice: SimulationState): SimulationState =
    val filteredSlice = slice.copy(
      customers = slice.customers.filterNot(_.isPlaying),
      games = slice.games.filterNot(_.isFull)
    )
    val updatedState = filteredSlice | manager
    slice.copy(
      customers = slice.customers.map(c =>
        updatedState.customers.find(_.id == c.id).getOrElse(c)
      ),
      games =
        slice.games.map(g => updatedState.games.find(_.id == g.id).getOrElse(g))
    )
