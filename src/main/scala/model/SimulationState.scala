package model

import model.customers.Customer
import model.entities.games.Game

case class SimulationState(customers: Seq[Customer], games: Seq[Game]):
  def update(): SimulationState =
    customers.foldLeft(this)((state, customer) => customer.update(state))
