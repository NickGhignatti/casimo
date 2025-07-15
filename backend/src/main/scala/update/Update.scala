package update

import scala.annotation.tailrec
import scala.util.Random

import model.GlobalConfig
import model.SimulationState
import model.data.DataManager
import model.entities.customers.Customer
import model.entities.customers.DefaultMovementManager
import model.entities.games.GameResolver
import model.entities.spawner.Spawner
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
        state.copy(
          spawner =
            Some(Spawner(Random.nextString(12), Vector2D(20.0, 10.0), n, 10)),
        )
