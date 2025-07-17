package update

import scala.annotation.tailrec
import scala.util.Random
import model.GlobalConfig
import model.SimulationState
import model.data.DataManager
import model.entities.Wall
import model.entities.customers.Customer
import model.entities.games.GameResolver
import model.entities.spawner.GaussianStrategy
import model.entities.spawner.Spawner
import model.managers.BaseManager
import model.managers.|
import update.Event.*
import utils.Vector2D

import scala.+:

case class Update(customerManager: BaseManager[SimulationState]):

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
        given GlobalConfig = GlobalConfig()
        update(state | customerManager, UpdateGames)

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
          spawner = Some(
            Spawner(
              Random.nextString(12),
              Vector2D(200.0, 200.0),
              GaussianStrategy(100, 6, 2)
            )
          )
        )

      case UpdateWalls(walls: List[Wall]) =>
        state.copy(walls = walls)
