package model

import model.customers.Customer
import model.games.Game

case class SimulationState(customers: List[Customer], games: List[Game])
