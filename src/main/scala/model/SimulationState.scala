package model

import model.entities.customers.Customer
import model.entities.games.Game

case class SimulationState(customers: List[Customer], games: List[Game])
