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
import model.managers.|
import update.Event._
import utils.Vector2D

case class Update(customerManager: DefaultMovementManager):

  def updateSimulationManager(
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
        update(state, UpdateCustomersState)

      case UpdateCustomersState =>
        state

      case AddCustomers(strategy) =>
        state.copy(
          spawner = Some(
            Spawner(
              "Spawner",
              Vector2D(200.0, 200.0),
              strategy
            )
          )
        )

      case UpdateWalls(walls: List[Wall]) =>
        state.copy(walls = walls)

      case updateGamesList(games: List[Game]) =>
        state.copy(games = games)
