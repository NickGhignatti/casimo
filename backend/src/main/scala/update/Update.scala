package update

import scala.annotation.tailrec

import model.SimulationState
import model.data.DataManager
import model.entities.Wall
import model.entities.customers.Customer
import model.entities.customers.DefaultMovementManager
import model.entities.games.Game
import model.entities.games.GameResolver
import model.entities.spawner.Spawner
import model.managers.CustomerBankrollManager
import model.managers.PersistenceManager
import model.managers.|
import model.setSpawner
import update.Event._
import utils.Vector2D

case class Update(customerManager: DefaultMovementManager):

  def updateSimulationDataManager(
      dataManager: DataManager,
      state: SimulationState
  ): DataManager = dataManager.copy(state = state)

  @tailrec
  final def update(state: SimulationState, event: Event): SimulationState =
    event match
      case SimulationTick =>
        state.spawner match
          case None => update(state, UpdateCustomersPosition)
          case Some(value) =>
            update(value.spawn(state), UpdateCustomersPosition)

      case UpdateCustomersPosition =>
        update(state | customerManager, UpdateGames)

      case UpdateGames =>
        val updatedGames =
          GameResolver.update(state.customers.toList, state.games)
        update(state.copy(games = updatedGames), UpdateSimulationBankrolls)

      case UpdateSimulationBankrolls =>
        val updatedBankroll =
          CustomerBankrollManager[Customer](state.games).update(state.customers)
        update(state.copy(customers = updatedBankroll), UpdateCustomersState)

      case UpdateCustomersState =>
        /*val updatedCustomerStrategy =
          CustomerStrategyManager[Customer](state.games).update(state.customers)*/
        val updatedCustomerState =
          PersistenceManager[Customer]().update(state.customers)
        state.copy(customers = updatedCustomerState)

      case AddCustomers(strategy) =>
        state.setSpawner(
          Spawner(
            "Spawner",
            Vector2D(200.0, 200.0),
            strategy,
            0.0,
            20.0
          )
        )

      case UpdateWalls(walls: List[Wall]) =>
        state.copy(walls = walls)

      case updateGamesList(games: List[Game]) =>
        state.copy(games = games)

      case ResetSimulation => SimulationState.empty()
