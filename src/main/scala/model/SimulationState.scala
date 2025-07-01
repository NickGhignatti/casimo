package model

import model.customers.Customer
import model.entities.games.Game

case class SimulationState[C <: Customer](customers: List[C], games: List[Game])
