package update

import model.SimulationState
import org.scalatest.funsuite.AnyFunSuite

class TestUpdate extends AnyFunSuite:

  test("update should leave state unchanged when no events affect it"):
    val initialState: SimulationState = SimulationState(List(), List(), None)
    val endState = Update.update(initialState, Event.SimulationTick)
    assert(endState === initialState)
