package model.managers

import model.entities.Player
import model.entities.customers.Bankroll
import model.entities.customers.BoredomFrustration
import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.customers.CustomerState
import model.entities.customers.HasBetStrategy
import model.entities.customers.RiskProfile
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
          val modifiers = riskProfileModifiers(c.riskProfile)
          val bankrollRatio = c.bankroll / c.startingBankroll

          if c.boredom > bThreshold * modifiers.boredomModifier ||
            c.frustration > fThreshold * modifiers.frustrationModifier ||
            c.betStrategy.betAmount > c.bankroll ||
            bankrollRatio > modifiers.takeProfit ||
            bankrollRatio < modifiers.stopLoss
          then
            val newC = c.stopPlaying.updateBoredom(
              -15.0 * modifiers.boredomModifier
            )
            newC.changeState(Idle).stopPlaying
          else c.updateBoredom(3.0 * modifiers.boredomModifier)

        case Idle => c
    }

  private val riskProfileModifiers: Map[RiskProfile, RiskProfileModifiers] =
    Map(
      VIP -> RiskProfileModifiers(
        takeProfit = 3.0,
        stopLoss = 0.3,
        boredomModifier = 1.30,
        frustrationModifier = 0.80
      ),
      Regular -> RiskProfileModifiers(
        takeProfit = 2.5,
        stopLoss = 0.3,
        boredomModifier = 1.0,
        frustrationModifier = 1.0
      ),
      Casual -> RiskProfileModifiers(
        takeProfit = 1.5,
        stopLoss = 0.5,
        boredomModifier = 1.40,
        frustrationModifier = 1.30
      ),
      Impulsive -> RiskProfileModifiers(
        takeProfit = 5.0,
        stopLoss = 0.0,
        boredomModifier = 0.70,
        frustrationModifier = 1.5
      )
    )
  private case class RiskProfileModifiers(
      takeProfit: Double,
      stopLoss: Double,
      boredomModifier: Double,
      frustrationModifier: Double
  )
