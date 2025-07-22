package update

import model.SimulationState
import model.data.DataManager
import model.entities.customers.{Customer, DefaultMovementManager}
import model.entities.games.GameBuilder
import org.scalatest.funsuite.AnyFunSuite
import update.Event.{ResetSimulation, updateGamesList}
import utils.Vector2D

class TestUpdate extends AnyFunSuite:
  val initState: SimulationState =
    SimulationState(List.empty, List.empty, None, List.empty)

  test("update should leave state unchanged when no events affect it"):
    val update = Update(DefaultMovementManager())
    val endState = update.update(initState, Event.SimulationTick)
    assert(endState === initState)

  test("update should update the data manager"):
    val manager = DataManager(initState)
    val finalState =
      SimulationState(
        List.empty,
        List.fill(1)(GameBuilder.slot(Vector2D.zero)),
        None,
        List.empty
      )
    val update = Update(DefaultMovementManager())
    assert(
      update
        .updateSimulationDataManager(manager, finalState)
        .state == finalState
    )

  test("update should update the games when added"):
    val update = Update(DefaultMovementManager())
    val slot = GameBuilder.slot(Vector2D.zero)
    val firstState =
      update.update(
        initState,
        updateGamesList(List(slot))
      )
    assert(firstState.games == List(slot))
    val roulette = GameBuilder.roulette(Vector2D.zero)
    val secondState =
      update.update(firstState, updateGamesList(List(slot, roulette)))
    assert(secondState.games == List(slot, roulette))

  test("reset should return a new empty simulation"):
    val simulationState = SimulationState(
      Seq(Customer().withId("test1")),
      List(GameBuilder.slot(Vector2D.zero)),
      None,
      List.empty
    )
    val update = Update(DefaultMovementManager())
    assert(
      SimulationState.empty() == update.update(simulationState, ResetSimulation)
    )
