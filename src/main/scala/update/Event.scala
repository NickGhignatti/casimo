package update

enum Event:
  case SimulationTick
  case UpdateCustomersPosition
  case UpdateGames
  case UpdateSimulationBankrolls
  case UpdateCustomersState
  case AddCustomers(num: Int)
