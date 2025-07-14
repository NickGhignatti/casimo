package update

import scala.annotation.tailrec
import scala.util.Random

import model.GlobalConfig
import model.SimulationState
import model.data.DataManager
import model.entities.Spawner
import model.entities.customers.Customer
import model.entities.customers.DefaultMovementManager
import model.entities.games.GameResolver
import model.managers.|
import update.Event._
import utils.Vector2D

object Update:

  def updateSimulationManager(
      dataManager: DataManager,
      state: SimulationState
  ): DataManager = dataManager.copy(state = state)

  @tailrec
  def update(state: SimulationState, event: Event): SimulationState =
    event match
      case SimulationTick =>
        state.spawner match
          case None => update(state, UpdateCustomersPosition)
          case Some(value) if value.customerQuantity == state.customers.size =>
            update(state, UpdateCustomersPosition)
          case Some(value) =>
            update(value.spawn(state), UpdateCustomersPosition)

      case UpdateCustomersPosition =>
        given GlobalConfig = GlobalConfig()
        update(state | DefaultMovementManager(), UpdateGames)

      case UpdateGames =>
        val updatedGames =
          GameResolver.update(state.customers.toList, state.games)
        update(state.copy(games = updatedGames), UpdateSimulationBankrolls)

      case UpdateSimulationBankrolls =>
        update(state, UpdateCustomersState)

      case UpdateCustomersState =>
        state

      case AddCustomers(n) =>
        println("Adding customers to the state...")
        val newCustomers = List.fill(50)(
          Customer(
            s"customer-${Random.nextInt()}",
            position = Vector2D(
              x = Random.between(10.0, 750.0),
              y = Random.between(10.0, 450.0)
            ),
            direction =
              Vector2D(Random.between(-50, 50), Random.between(-50, 50)),
            bankroll = Random.between(30, 5000)
          )
        )
        state.copy(
          customers = state.customers ++ newCustomers,
          spawner =
            Some(Spawner(Random.nextString(12), Vector2D(20.0, 10.0), n, 10)),
        )
