package update

import model.SimulationState
import org.junit.Assert.assertEquals
import org.junit.Test

class TestUpdate {
  val initialState: SimulationState = model.SimulationState(List(), List())

  @Test
  def testUpdateWithoutStateChanges(): Unit = {
    val endState = Update.update(initialState, Event.SimulationTick)
    assertEquals(initialState, endState)
  }
}
