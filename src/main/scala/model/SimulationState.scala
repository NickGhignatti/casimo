package model

import model.entities.customers.{Customer, CustomerList}
import model.entities.games.Game

case class SimulationState(customers: CustomerList, games: List[Game])
