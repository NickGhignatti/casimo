package update

import model.entities.Wall
import model.entities.games.Game

enum Event:
  case SimulationTick
  case UpdateCustomersPosition
  case UpdateGames
  case UpdateSimulationBankrolls
  case UpdateCustomersState
  case AddCustomers(num: Int)
  case UpdateWalls(walls: List[Wall])
  case updateGamesList(games: List[Game])
