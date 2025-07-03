package update

import org.scalatest.funsuite.AnyFunSuite
import model.SimulationState
import model.customers.Customer

class TestUpdate extends AnyFunSuite:

  test("update should leave state unchanged when no events affect it"):
    val initialState = SimulationState(List(), List())
    val endState = Update.update(initialState, Event.SimulationTick)
    assert(endState === initialState)
