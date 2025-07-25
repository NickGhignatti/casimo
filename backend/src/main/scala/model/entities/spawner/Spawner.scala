package model.entities.spawner

import scala.util.Random

import model.SimulationState
import model.entities.Entity
import model.entities.customers.Customer
import model.entities.customers.FlatBetting
import model.entities.customers.MartingaleStrat
import model.entities.customers.RiskProfile.Casual
import model.entities.customers.RiskProfile.Impulsive
import model.entities.customers.RiskProfile.Regular
import model.entities.customers.RiskProfile.VIP
import model.entities.customers.defaultRedBet
import model.entities.games.Blackjack
import model.entities.games.Roulette
import model.entities.games.SlotMachine
import utils.Vector2D

/** Entity responsible for spawning new customers into the simulation.
  *
  * The Spawner periodically creates new Customer instances based on its
  * configured spawning strategy and timing parameters. It maintains its own
  * simulation time counter and determines when to spawn customers based on the
  * ticksToSpawn interval.
  *
  * Spawned customers are created with randomized attributes including position,
  * movement direction, bankroll, and preferred game type to simulate realistic
  * diversity in the casino environment.
  *
  * @param id
  *   unique identifier for this spawner instance
  * @param position
  *   2D coordinates where customers will be spawned (with random variation)
  * @param strategy
  *   the spawning strategy that determines how many customers to create
  * @param currentTime
  *   internal simulation time counter for this spawner
  * @param ticksToSpawn
  *   interval in simulation ticks between spawning events
  */
case class Spawner(
    id: String,
    position: Vector2D,
    strategy: SpawningStrategy
) extends Entity:

  /** Processes a simulation tick, potentially spawning new customers.
    *
    * Checks if the current time interval matches the spawning schedule and, if
    * so, creates new customers according to the spawning strategy.
    *
    * @param state
    *   the current simulation state containing customers and spawner
    * @return
    *   updated simulation state with potentially new customers and updated
    *   spawner time
    */
  def spawn(
      state: SimulationState
  ): SimulationState =
    if state.ticker.isReadyToSpawn then
      state.copy(
        customers = state.customers ++ Seq.fill(
          strategy.customersAt(
            state.ticker.currentTick / state.ticker.spawnTick
          )
        )(defaultCustomerCreation())
      )
    else state

  def defaultCustomerCreation(): Customer =
    val br = Random.between(50, 10000)
    val p = br match
      case b if b < 100   => Casual
      case b if b < 1500  => Regular
      case b if b < 5000  => Impulsive
      case b if b < 10000 => VIP
    val fg = Random
      .shuffle(
        Seq(
          Roulette,
          Blackjack,
          SlotMachine
        )
      )
      .head
    val bs = fg match
      case Roulette    => MartingaleStrat[Customer](br * 0.02, defaultRedBet)
      case Blackjack   => MartingaleStrat[Customer](br * 0.02, defaultRedBet)
      case SlotMachine => FlatBetting[Customer](br * 0.04)

    Customer()
      .withPosition(this.position.around(5.0))
      .withDirection(Vector2D(Random.between(0, 5), Random.between(0, 5)))
      .withBankroll(br)
      .withFavouriteGames(fg)
      .withProfile(p)
      .withBetStrategy(bs)
