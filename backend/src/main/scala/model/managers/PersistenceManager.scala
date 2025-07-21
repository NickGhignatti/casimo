package model.managers

import model.entities.Player
import model.entities.customers.Bankroll
import model.entities.customers.BoredomFrustration
import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.customers.CustomerState
import model.entities.customers.HasBetStrategy
import model.entities.customers.RiskProfile.Casual
import model.entities.customers.RiskProfile.Impulsive
import model.entities.customers.RiskProfile.Regular
import model.entities.customers.RiskProfile.VIP
import model.entities.customers.StatusProfile

case class PersistenceManager[
    A <: BoredomFrustration[A] & CustomerState[A] & Bankroll[A] &
      HasBetStrategy[A] & Player[A] & StatusProfile
](bThreshold: Double = 80, fThreshold: Double = 60)
    extends BaseManager[Seq[A]]:
  def update(customers: Seq[A]): Seq[A] =
    customers.map { c =>
      c.customerState match
        case Playing(game) =>
          val takeProfit = c.riskProfile match
            case VIP       => 2
            case Regular   => 1.5
            case Casual    => 1.5
            case Impulsive => 5
          val stopLoss = c.riskProfile match
            case VIP       => 0.3
            case Regular   => 0.5
            case Casual    => 0.3
            case Impulsive => 0
          val bankrollRatio = c.bankroll / c.startingBankroll
          if c.boredom > bThreshold || c.frustration > fThreshold || c.betStrategy.betAmount > c.bankroll || bankrollRatio > takeProfit || bankrollRatio < stopLoss
          then
            val newC = c.stopPlaying
            newC.changeState(Idle)
          else c
        case Idle => c
    }
