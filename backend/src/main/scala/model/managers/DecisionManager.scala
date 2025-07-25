package model.managers

import model.entities.Entity
import model.entities.Player
import model.entities.customers.Bankroll
import model.entities.customers.BetStratType
import model.entities.customers.BettingStrategy
import model.entities.customers.BoredomFrustration
import model.entities.customers.CustState.Idle
import model.entities.customers.CustomerState
import model.entities.customers.FlatBet
import model.entities.customers.FlatBetting
import model.entities.customers.HasBetStrategy
import model.entities.customers.Martingale
import model.entities.customers.MartingaleStrat
import model.entities.customers.MovableWithPrevious
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
import utils.TriggerDSL.BoredomAbove
import utils.TriggerDSL.BrRatioAbove
import utils.TriggerDSL.BrRatioBelow
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
    case class Modifiers(limits: Limits, bMod: Double, fMod: Double)
    val modifiers: Map[RiskProfile, Modifiers] = Map(
      RiskProfile.VIP -> Modifiers(Limits(tp = 3.0, sl = 0.3), 1.30, 0.80),
      RiskProfile.Regular -> Modifiers(Limits(2.5, 0.3), 1.0, 1.0),
      RiskProfile.Casual -> Modifiers(Limits(1.5, 0.5), 1.40, 1.30),
      RiskProfile.Impulsive -> Modifiers(Limits(5.0, 0.0), 0.70, 1.5)
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
  case class ContinuePlaying() extends CustomerDecision
  case class StopPlaying() extends CustomerDecision
  case class ChangeStrategy(newStrategy: BettingStrategy[A])
      extends CustomerDecision
  case class Stay() extends CustomerDecision
  case class LeaveCasino() extends CustomerDecision
  case class WaitForGame() extends CustomerDecision

  def update(customers: Seq[A]): Seq[A] =
    val tree = buildDecisionTree

    customers.flatMap { c =>
      val mod = ProfileModifiers.modifiers(c.riskProfile)
      val decision = tree.eval(c)

      decision match
        case ContinuePlaying() =>
          Some(updateInGameBehaviours(c).updateBoredom(3.0 * mod.bMod))
        case StopPlaying() =>
          Some(c.changeState(Idle).updateFrustration(-15.0 * (2 - mod.fMod)))
        case ChangeStrategy(s) =>
          Some(c.changeBetStrategy(s).updateBoredom(-15.0 * (2 - mod.bMod)))
        case WaitForGame() => Some(c)
        case Stay()        => Some(c)
        case LeaveCasino() => None
    }

  private def updateInGameBehaviours(c: A): A =
    val updatedGame = games.find(_.id == c.getGameOrElse.get.id).get
    val lastRound = updatedGame.getLastRoundResult
    lastRound.find(_.getCustomerWhichPlayed == c.id) match
      case Some(g) => c.updateAfter(-g.getMoneyGain)
      case _       => c

  // === Tree Builders ===
  private def buildDecisionTree: DecisionTree[A, CustomerDecision] =
    DecisionNode[A, CustomerDecision](
      predicate = _.isPlaying,
      trueBranch = gameNode,
      falseBranch = leaveStayNode
    )

  private def gameNode: DecisionTree[A, CustomerDecision] =
    def checkIfPlaying(c: A): Boolean =
      val updatedGame = games.find(_.id == c.getGameOrElse.get.id).get
      val lastRound = updatedGame.getLastRoundResult
      lastRound.nonEmpty

    DecisionNode[A, CustomerDecision](
      predicate = c => checkIfPlaying(c),
      trueBranch = profileNode,
      falseBranch = Leaf[A, CustomerDecision](c => WaitForGame())
    )

  private def profileNode: DecisionTree[A, CustomerDecision] =
    MultiNode[A, RiskProfile, CustomerDecision](
      keyOf = _.riskProfile,
      branches = RiskProfile.values.map(p => p -> stopContinueNode(p)).toMap,
      default = Leaf[A, CustomerDecision](c => StopPlaying())
    )

  private def leaveStayNode: DecisionTree[A, CustomerDecision] =
    def leaveRequirements(c: A): Boolean =
      val mod = ProfileModifiers.modifiers(c.riskProfile)
      val r = c.bankroll / c.startingBankroll
      val trigger: Trigger[A] = BoredomAbove(
        (80 * mod.bMod).max(100.0)
      ) || FrustAbove((80 * mod.fMod).max(100.0))
        || BrRatioAbove(mod.limits.tp) || BrRatioBelow(mod.limits.sl)
      trigger.eval(c)
    DecisionNode[A, CustomerDecision](
      predicate = c => leaveRequirements(c),
      trueBranch = Leaf[A, CustomerDecision](c => LeaveCasino()),
      falseBranch = Leaf[A, CustomerDecision](c => Stay())
    )

  private def stopContinueNode(
      profile: RiskProfile,
      bThreshold: Double = 70,
      fThreshold: Double = 60
  ): DecisionTree[A, CustomerDecision] =
    def stopPlayingRequirements(c: A): Boolean =
      val mod = ProfileModifiers.modifiers(profile)
      val r = c.bankroll / c.startingBankroll
      val trigger: Trigger[A] =
        BoredomAbove(bThreshold * mod.bMod) || FrustAbove(fThreshold * mod.fMod)
          || BrRatioAbove(mod.limits.tp) || BrRatioBelow(mod.limits.sl)
      updateInGameBehaviours(c).betStrategy.betAmount > c.bankroll || trigger
        .eval(c)

    DecisionNode[A, CustomerDecision](
      predicate = c => stopPlayingRequirements(c),
      trueBranch = Leaf[A, CustomerDecision](c => StopPlaying()),
      falseBranch = strategySwitchNode(profile)
    )

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
            ChangeStrategy(betDefiner(rule, c))
        }
        .getOrElse(ContinuePlaying())
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

object PostDecisionUpdater:
  def updatePosition[P <: MovableWithPrevious[P] & CustomerState[P] & Entity](
      before: Seq[P],
      post: Seq[P]
  ): List[P] =
    val beforeMap = before.map(p => p.id -> p).toMap
    val postMap = post.map(p => p.id -> p).toMap

    val remained = beforeMap.keySet.intersect(postMap.keySet)

    val (hasStopPlaying, unchangedState) = remained.toList
      .map(id => (beforeMap(id), postMap(id)))
      .partition { case (oldState, newState) =>
        oldState.isPlaying != newState.isPlaying
      }

    val changePosition = hasStopPlaying.map { case (_, newP) =>
      newP
        .withPosition(newP.previousPosition.getOrElse(newP.position))
        .withDirection(-newP.direction)
    }
    val unchanged = unchangedState.map(_._2)
    changePosition ++ unchanged
