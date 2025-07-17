package update

import model.entities.Wall

enum Event:
  case SimulationTick
  case UpdateCustomersPosition
  case UpdateGames
  case UpdateSimulationBankrolls
  case UpdateCustomersState
  case AddCustomers(num: Int)
  case UpdateWalls(walls: List[Wall])
