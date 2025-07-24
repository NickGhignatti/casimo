package update

import model.entities.Wall
import model.entities.games.Game
import model.entities.spawner.SpawningStrategy

enum Event:
  case SimulationTick
  case SpawnCustomers
  case UpdateCustomersPosition
  case UpdateGames
  case UpdateSimulationBankrolls
  case UpdateCustomersState
  case AddCustomers(strategy: SpawningStrategy)
  case UpdateWalls(walls: List[Wall])
  case updateGamesList(games: List[Game])
  case ResetSimulation
  case BorderConfig(x: Double, y: Double, width: Double, height: Double)
