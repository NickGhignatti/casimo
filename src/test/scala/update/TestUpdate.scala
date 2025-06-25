package update

import org.scalatest.funsuite.AnyFunSuite
import model.SimulationState

class TestUpdate extends AnyFunSuite

  val initialState: SimulationState = SimulationState(List(), List())

  test("update should leave state unchanged when no events affect it")
    val endState = Update.update(initialState, Event.SimulationTick)
    assert(endState === initialState)



