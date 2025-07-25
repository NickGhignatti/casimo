package model.entities.spawner

import scala.util.Random

import model.SimulationState
import model.entities.Entity
import model.entities.customers.Customer
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
        )(
          Customer(
            id = "cutomer-" + Random.nextInt(),
            position = this.position.around(5.0),
            direction = Vector2D(Random.between(0, 5), Random.between(0, 5)),
            bankroll = Random.between(30, 5000),
            favouriteGame = Random
              .shuffle(
                Seq(
                  model.entities.games.Roulette,
                  model.entities.games.Blackjack,
                  model.entities.games.SlotMachine
                )
              )
              .head
          )
        )
      )
    else state
