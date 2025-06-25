package update

import model.SimulationState
import update.Event.*

import scala.annotation.tailrec

object Update:

  @tailrec
  def update(state: SimulationState, event: Event): SimulationState =
    event match
      case SimulationTick            => update(state, UpdateCustomersPosition)
      case UpdateCustomersPosition   => update(state, UpdateGames)
      case UpdateGames               => update(state, UpdateSimulationBankrolls)
      case UpdateSimulationBankrolls => update(state, UpdateCustomersState)
      case UpdateCustomersState      => state
