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

/** Central update processor for the casino simulation's event-driven
  * architecture.
  *
  * The Update class serves as the main event processor that handles all
  * simulation state transitions. It implements a tail-recursive event
  * processing system where events are chained together to form complete
  * simulation update cycles. Each simulation tick triggers a cascade of events
  * that update different aspects of the simulation in a controlled sequence.
  *
  * The class coordinates between various managers (movement, bankroll,
  * decision) to ensure consistent state updates across all simulation
  * components.
  *
  * @param customerManager
  *   the movement manager responsible for customer positioning and collision
  *   detection
  */
case class Update(customerManager: DefaultMovementManager):

  /** Updates a DataManager instance with new simulation state.
    *
    * Provides a convenient way to synchronize DataManager instances with
    * changes to the simulation state, ensuring data consistency across
    * different parts of the system.
    *
    * @param dataManager
    *   the current DataManager instance
    * @param state
    *   the updated simulation state
    * @return
    *   new DataManager instance with the updated state
    */
  def updateSimulationDataManager(
      dataManager: DataManager,
      state: SimulationState
  ): DataManager = dataManager.copy(state = state)

  /** Processes simulation events using tail-recursive event chaining.
    *
    * This is the core method of the simulation update system. It processes
    * events in a controlled sequence, where each event type triggers specific
    * updates and then chains to the next appropriate event. The tail-recursive
    * implementation ensures stack safety during long simulation runs.
    *
    * The event processing flow follows this typical sequence:
    *   1. SimulationTick → SpawnCustomers → UpdateCustomersPosition →
    *      UpdateGames → UpdateSimulationBankrolls → UpdateCustomersState →
    *      (complete)
    *
    * Configuration events (AddCustomers, UpdateWalls, etc.) typically complete
    * without chaining to other events.
    *
    * @param state
    *   the current simulation state
    * @param event
    *   the event to process
    * @return
    *   the updated simulation state after processing the event and any chained
    *   events
    */
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
