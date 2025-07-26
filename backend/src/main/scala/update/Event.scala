package update

import model.entities.Wall
import model.entities.games.Game
import model.entities.spawner.SpawningStrategy

/** Enumeration of all possible events that can occur in the casino simulation.
  *
  * Events represent discrete actions or state changes that drive the simulation
  * forward. They follow an event-driven architecture pattern where different
  * parts of the system can trigger events, and event handlers process them to
  * update the simulation state accordingly.
  *
  * Events are categorized into regular simulation updates (ticks, position
  * updates), configuration changes (walls, games), and control actions (reset,
  * spawning).
  */
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
