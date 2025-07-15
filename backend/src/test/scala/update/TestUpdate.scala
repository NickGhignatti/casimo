package update

import model.SimulationState
import model.entities.customers.DefaultMovementManager
import model.data.DataManager
import model.entities.games.GameBuilder
import org.scalatest.funsuite.AnyFunSuite
import utils.Vector2D

class TestUpdate extends AnyFunSuite:

  test("update should leave state unchanged when no events affect it"):
    val initialState: SimulationState = SimulationState(List(), List(), None)
    val update = Update(DefaultMovementManager())
    val endState = update.update(initialState, Event.SimulationTick)
    assert(endState === initialState)

  test("update should update the data manager"):
    val initState = SimulationState(List.empty, List.empty, None)
    val manager = DataManager(initState)
    val finalState =
      SimulationState(
        List.empty,
        List.fill(1)(GameBuilder.slot(Vector2D.zero)),
        None
      )
    val update = Update(DefaultMovementManager())
    assert(
      update.updateSimulationManager(manager, finalState).state == finalState
    )
