package update

import model.SimulationState
import update.Event.*

import scala.annotation.tailrec

object Update:

  @tailrec
  def update(state: SimulationState, event: Event): SimulationState =
    event match
      case SimulationTick =>
        println("Simulation tick event received, updating state...")
        update(state, UpdateCustomersPosition)
      case UpdateCustomersPosition =>
        println("Updating customers' positions...")
        update(state, UpdateGames)
      case UpdateGames =>
        println("Updating games...")
        update(state, UpdateSimulationBankrolls)
      case UpdateSimulationBankrolls =>
        println("Updating simulation bankrolls...")
        update(state, UpdateCustomersState)
      case UpdateCustomersState =>
        println("Updating customers' state...")
        state
