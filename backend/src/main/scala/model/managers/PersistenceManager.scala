package model.managers

import model.entities.customers.Bankroll
import model.entities.customers.BoredomFrustration
import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.customers.CustomerState
import model.entities.customers.HasBetStrategy

case class PersistenceManager[
    A <: BoredomFrustration[A] & CustomerState[A] & Bankroll[A] &
      HasBetStrategy[A]
](bThreshold: Double = 80, fThreshold: Double = 60)
    extends BaseManager[Seq[A]]:
  def update(customers: Seq[A]): Seq[A] =
    customers.map { c =>
      c.customerState match
        case Playing(game) =>
          if c.boredom > bThreshold || c.frustration > fThreshold || c.betStrategy.betAmount > c.bankroll
          then c.changeState(Idle)
          else c
        case Idle => c
    }
