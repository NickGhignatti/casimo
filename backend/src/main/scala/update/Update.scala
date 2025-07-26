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
import model.managers.DecisionManager
import model.managers.PostDecisionUpdater
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
        update(state.copy(ticker = state.ticker.update()), SpawnCustomers)

      case SpawnCustomers =>
        state.spawner match
          case None =>
            update(
              state,
              UpdateCustomersPosition
            )
          case Some(value) =>
            update(
              value.spawn(state),
              UpdateCustomersPosition
            )

      case UpdateCustomersPosition =>
        update(state | customerManager, UpdateGames)

      case UpdateGames =>
        val updatedGames =
          GameResolver.update(state.customers.toList, state.games, state.ticker)
        update(state.copy(games = updatedGames), UpdateSimulationBankrolls)

      case UpdateSimulationBankrolls =>
        val updatedBankroll =
          CustomerBankrollManager[Customer](state.games).update(state.customers)
        update(state.copy(customers = updatedBankroll), UpdateCustomersState)

      case UpdateCustomersState =>
        val updatedCustomerState =
          DecisionManager[Customer](state.games).update(state.customers)
        val pDUPosition = PostDecisionUpdater.updatePosition(
          state.customers,
          updatedCustomerState
        )
        val pDUGames = PostDecisionUpdater.updateGames(
          state.customers,
          updatedCustomerState,
          state.games
        )
        state.copy(customers = pDUPosition, games = pDUGames)

      case AddCustomers(strategy) =>
        state.setSpawner(
          Spawner(
            "Spawner",
            Vector2D(10.0, 10.0),
            strategy
          )
        )

      case BorderConfig(x: Double, y: Double, width: Double, height: Double) =>
        SimulationState.base(x, y, width, height)

      case UpdateWalls(walls: List[Wall]) =>
        state.copy(walls = walls)

      case updateGamesList(games: List[Game]) =>
        state.copy(games = games)

      case ResetSimulation => SimulationState.empty()
