package model.managers

import model.entities.Entity
import model.entities.Player
import model.entities.customers.Bankroll
import model.entities.customers.BetStratType
import model.entities.customers.BettingStrategy
import model.entities.customers.BoredomFrustration
import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.customers.CustomerState
import model.entities.customers.FlatBet
import model.entities.customers.FlatBetting
import model.entities.customers.HasBetStrategy
import model.entities.customers.Martingale
import model.entities.customers.MartingaleStrat
import model.entities.customers.OscarGrind
import model.entities.customers.OscarGrindStrat
import model.entities.customers.RiskProfile
import model.entities.customers.RiskProfile.Impulsive
import model.entities.customers.RiskProfile.Regular
import model.entities.customers.RiskProfile.VIP
import model.entities.customers.StatusProfile
import model.entities.games.Blackjack
import model.entities.games.Gain
import model.entities.games.Game
import model.entities.games.GameType
import model.entities.games.Roulette
import model.entities.games.SlotMachine
import utils.DecisionNode
import utils.DecisionTree
import utils.Leaf
import utils.MultiNode
import utils.TriggerDSL.BrRatioAbove
import utils.TriggerDSL.FrustAbove
import utils.TriggerDSL.Losses
import utils.TriggerDSL.Trigger

case class DecisionManager[
    A <: Bankroll[A] & BoredomFrustration[A] & CustomerState[A] &
      HasBetStrategy[A] & Player[A] & Entity & StatusProfile
](games: List[Game])
    extends BaseManager[Seq[A]]:
  private val gameList = games.map(_.gameType).distinct
  // Configuration
  private object ProfileModifiers:
    case class Limits(tp: Double, sl: Double)
    val modifiers: Map[RiskProfile, (Limits, Double, Double)] = Map(
      RiskProfile.VIP -> (Limits(tp = 3.0, sl = 0.3), 1.30, 0.80),
      RiskProfile.Regular -> (Limits(2.5, 0.3), 1.0, 1.0),
      RiskProfile.Casual -> (Limits(1.5, 0.5), 1.40, 1.30),
      RiskProfile.Impulsive -> (Limits(5.0, 0.0), 0.70, 1.5)
    )
  // Rule & Future External Config
  case class SwitchRule(
      profile: RiskProfile,
      game: GameType,
      strategy: BetStratType,
      trigger: Trigger[A],
      nextStrategy: BetStratType,
      betPercentage: Double
  )
//format: off
  object DefaultConfig:
    val switchRules: List[SwitchRule] = List(
      // VIP
      SwitchRule(VIP, Blackjack, Martingale, Losses(3), OscarGrind, 0.05),
      SwitchRule(VIP, Roulette, Martingale, Losses(4), OscarGrind, 0.05),
      SwitchRule(VIP, SlotMachine, FlatBet, FrustAbove(50), FlatBet, 0.015),
      // Regular
      SwitchRule(Regular, Blackjack, OscarGrind, BrRatioAbove(1.3), Martingale, 0.015),
      SwitchRule(Regular, Blackjack, Martingale, Losses(3), OscarGrind, 0.02),
      SwitchRule(Regular, Roulette, OscarGrind, BrRatioAbove(1.3), Martingale, 0.015),
      SwitchRule(Regular, Roulette, Martingale, Losses(3), OscarGrind, 0.02),
      SwitchRule(Regular, SlotMachine, FlatBet, FrustAbove(60), FlatBet, 0.01),
      // Casual

      // Impulsive
      SwitchRule(Impulsive, Blackjack, Martingale, Losses(3), OscarGrind, 0.10),
      SwitchRule(Impulsive, Roulette, Martingale, Losses(3), FlatBet, 0.07),
      SwitchRule(Impulsive, Roulette, FlatBet, BrRatioAbove(1), Martingale, 0.03),
      SwitchRule(Impulsive, SlotMachine, FlatBet, FrustAbove(50), FlatBet, 0.02)
    )
//format: on
  object ConfigLoader:
    def load(): List[SwitchRule] = DefaultConfig.switchRules
  private lazy val rulesByProfile: Map[RiskProfile, List[SwitchRule]] =
    ConfigLoader.load().groupBy(_.profile)

  sealed trait CustomerDecision
  case class ContinuePlaying(customer: A) extends CustomerDecision
  case class StopPlaying(customer: A) extends CustomerDecision
  case class ChangeStrategy(customer: A, newStrategy: BettingStrategy[A])
      extends CustomerDecision

  def update(customers: Seq[A]): Seq[A] =
    val tree = buildDecisionTree
    val (playing, idle) = customers.partition(_.customerState match
      case Playing(_) => true
      case _          => false
    )
    val updated = playing.map(c =>
      val updatedGame = games.find(_.id == c.getGameOrElse.get.id).get
      val lastRound = updatedGame.getLastRoundResult
      val updatedC = lastRound.find(_.getCustomerWhichPlayed == c.id) match
        case Some(g) => c.updateAfter(g.getMoneyGain)
        case _       => c

      val decision = tree.eval(updatedC)

      decision match
        case ContinuePlaying(c)   => c
        case StopPlaying(c)       => c.changeState(Idle)
        case ChangeStrategy(c, s) => c.changeBetStrategy(s)
    )

    idle ++ updated

  // === Tree Builders ===
  private def buildDecisionTree: DecisionTree[A, CustomerDecision] =
    MultiNode[A, RiskProfile, CustomerDecision](
      keyOf = _.riskProfile,
      branches = RiskProfile.values.map(p => p -> stopContinueNode(p)).toMap,
      default = Leaf[A, CustomerDecision](c => StopPlaying(c))
    )

  // 1) Stop/Continue logic
  private def stopContinueNode(
      profile: RiskProfile,
      bThreshold: Double = 80,
      fThreshold: Double = 60
  ): DecisionTree[A, CustomerDecision] =
    def stopPlayingRequirements(c: A): Boolean =
      val (limits, bMod, fMod) = ProfileModifiers.modifiers(profile)
      val r = c.bankroll / c.startingBankroll
      c.boredom > bThreshold * bMod || c.frustration > fThreshold * fMod ||
      c.betStrategy.betAmount > c.bankroll || r > limits.tp || r < limits.sl

    DecisionNode[A, CustomerDecision](
      predicate = c => stopPlayingRequirements(c),
      trueBranch = Leaf[A, CustomerDecision](c => StopPlaying(c)),
      falseBranch = strategySwitchNode(profile)
    )

  // 2) BetStratType switch logic with priority & fallback
  private def strategySwitchNode(
      profile: RiskProfile
  ): DecisionTree[A, CustomerDecision] =
    val rules = rulesByProfile.getOrElse(profile, Nil)
    Leaf[A, CustomerDecision] { c =>
      rules
        .collectFirst {
          case rule
              if rule.game == c.getGameOrElse.get.gameType && rule.strategy == c.betStrategy.betType && rule.trigger
                .eval(c) =>
            ChangeStrategy(c, betDefiner(rule, c))
        }
        .getOrElse(ContinuePlaying(c))
    }

  private def betDefiner(rule: SwitchRule, c: A): BettingStrategy[A] =
    rule.nextStrategy match
      case FlatBet =>
        FlatBetting(c.bankroll * rule.betPercentage, c.betStrategy.option)
      case Martingale =>
        MartingaleStrat(c.bankroll * rule.betPercentage, c.betStrategy.option)
      case OscarGrind =>
        OscarGrindStrat(
          c.bankroll * rule.betPercentage,
          c.bankroll,
          c.betStrategy.option
        )
