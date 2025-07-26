package model.managers

import scala.util.Random

import model.entities.ChangingFavouriteGamePlayer
import model.entities.Entity
import model.entities.Player
import model.entities.customers.Bankroll
import model.entities.customers.BetStratType
import model.entities.customers.BettingStrategy
import model.entities.customers.BoredomFrustration
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
import model.entities.customers.RiskProfile.Casual
import model.entities.customers.RiskProfile.Impulsive
import model.entities.customers.RiskProfile.Regular
import model.entities.customers.RiskProfile.VIP
import model.entities.customers.StatusProfile
import model.entities.customers.defaultRedBet
import model.entities.games.Blackjack
import model.entities.games.Gain
import model.entities.games.Game
import model.entities.games.GameType
import model.entities.games.Roulette
import model.entities.games.SlotMachine
import model.entities.games.gameTypesPresent
import utils.DecisionNode
import utils.DecisionTree
import utils.Leaf
import utils.MultiNode
import utils.TriggerDSL.Always
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
  private case class Limits(tp: Double, sl: Double)
  private case class Modifiers(limits: Limits, bMod: Double, fMod: Double)
  private object ProfileModifiers:
    val modifiers: Map[RiskProfile, Modifiers] = Map(
      RiskProfile.VIP -> Modifiers(Limits(tp = 3.0, sl = 0.3), 1.30, 0.80),
      RiskProfile.Regular -> Modifiers(Limits(2.5, 0.3), 1.0, 1.0),
      RiskProfile.Casual -> Modifiers(Limits(1.5, 0.5), 1.40, 1.30),
      RiskProfile.Impulsive -> Modifiers(Limits(5.0, 0.1), 0.70, 1.5)
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
      SwitchRule(VIP, SlotMachine, FlatBet, FrustAbove(50) || BrRatioBelow(0.5), FlatBet, 0.015),
      // Regular
      SwitchRule(Regular, Blackjack, OscarGrind, BrRatioAbove(1.3), Martingale, 0.015),
      SwitchRule(Regular, Blackjack, Martingale, Losses(3), OscarGrind, 0.02),
      SwitchRule(Regular, Roulette, OscarGrind, BrRatioAbove(1.3), Martingale, 0.015),
      SwitchRule(Regular, Roulette, Martingale, Losses(3), OscarGrind, 0.02),
      SwitchRule(Regular, SlotMachine, FlatBet, FrustAbove(60) || BrRatioBelow(0.5), FlatBet, 0.01),
      // Casual
      SwitchRule(Casual, SlotMachine, FlatBet, FrustAbove(50) || BrRatioBelow(0.7), FlatBet, 0.015),
      // Impulsive
      SwitchRule(Impulsive, Blackjack, Martingale, Losses(3), OscarGrind, 0.10),
      SwitchRule(Impulsive, Roulette, Martingale, Losses(3), FlatBet, 0.07),
      SwitchRule(Impulsive, Roulette, FlatBet, BrRatioAbove(1), Martingale, 0.03),
      SwitchRule(Impulsive, SlotMachine, FlatBet, FrustAbove(50), FlatBet, 0.02),

      SwitchRule(VIP,SlotMachine,Martingale, Always ,FlatBet,0.03),
      SwitchRule(Impulsive,SlotMachine,Martingale, Always ,FlatBet,0.03),
      SwitchRule(Casual,SlotMachine,Martingale, Always ,FlatBet,0.03),
      SwitchRule(Regular,SlotMachine,Martingale, Always ,FlatBet,0.03),
      SwitchRule(VIP,SlotMachine,OscarGrind, Always ,FlatBet,0.03),
      SwitchRule(Impulsive,SlotMachine,OscarGrind, Always ,FlatBet,0.03),
      SwitchRule(Casual,SlotMachine,OscarGrind, Always ,FlatBet,0.03),
      SwitchRule(Regular,SlotMachine,OscarGrind, Always ,FlatBet,0.03),

    )
//format: on
  object ConfigLoader:
    def load(): List[SwitchRule] = DefaultConfig.switchRules
  lazy val rulesByProfile: Map[RiskProfile, List[SwitchRule]] =
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
          Some(updateInGameBehaviours(c, mod).updateBoredom(3.0 * mod.bMod))
        case StopPlaying() =>
          Some(c.stopPlaying.updateFrustration(-15.0 * (2 - mod.fMod)))
        case ChangeStrategy(s) =>
          Some(c.changeBetStrategy(s).updateBoredom(-15.0 * (2 - mod.bMod)))
        case WaitForGame() => Some(getNewGameBet(c))
        case Stay()        => Some(c)
        case LeaveCasino() => None
    }

  private def getNewGameBet(c: A): A =
    val rules = rulesByProfile(c.riskProfile)
    rules
      .collectFirst {
        case rule
            if rule.game == c.getGameOrElse.get.gameType && rule.strategy == c.betStrategy.betType && rule.trigger
              .eval(c) =>
          c.changeBetStrategy(betDefiner(rule, c))
      }
      .getOrElse(
        c.changeBetStrategy(FlatBetting(c.bankroll * 0.01, defaultRedBet))
      )

  private def updateInGameBehaviours(c: A, mod: Modifiers): A =
    val updatedGame = games.find(_.id == c.getGameOrElse.get.id).get
    val lastRound = updatedGame.getLastRoundResult
    lastRound.find(_.getCustomerWhichPlayed == c.id) match
      case Some(g) =>
        if g.getMoneyGain > 0 then
          c.updateFrustration(
            (5 / c.bankrollRatio.max(0.5).min(2.0)) * mod.fMod
          ).updateAfter(-g.getMoneyGain)
        else
          c.updateFrustration(
            (-5 / c.bankrollRatio.max(0.5).min(2.0)) * (2 - mod.fMod)
          ).updateAfter(-g.getMoneyGain)

      case _ => c

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
      val trigger: Trigger[A] = BoredomAbove(
        (80 * mod.bMod).min(95.0)
      ) || FrustAbove((80 * mod.fMod).min(95.0))
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
      val betAmount = updateInGameBehaviours(c, mod).betStrategy.betAmount
      val trigger: Trigger[A] =
        BoredomAbove(bThreshold * mod.bMod) || FrustAbove(fThreshold * mod.fMod)
          || BrRatioAbove(mod.limits.tp) || BrRatioBelow(mod.limits.sl)
      betAmount > c.bankroll - 1 || trigger.eval(c)

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

  def betDefiner(rule: SwitchRule, c: A): BettingStrategy[A] =
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
  def updatePosition[
      P <: MovableWithPrevious[P] & CustomerState[P] &
        ChangingFavouriteGamePlayer[P] & Entity
  ](
      before: Seq[P],
      post: Seq[P]
  ): List[P] =
    val (hasStopPlaying, unchangedState, remained) =
      groupForChangeOfState[P](before, post)

    val changePosition = hasStopPlaying.map { case (oldP, newP) =>
      newP
        .withPosition(oldP.previousPosition.get)
        .withDirection(-newP.direction)
        .withFavouriteGame(
          Random.shuffle(gameTypesPresent.filter(_ != newP.favouriteGame)).head
        )
    }
    val unchanged = unchangedState.map(_._2)
    changePosition ++ unchanged

  def updateGames[P <: CustomerState[P] & Entity](
      before: Seq[P],
      post: Seq[P],
      games: List[Game]
  ): List[Game] =
    val (hasStopPlaying, unchangedState, remained) =
      groupForChangeOfState(before, post)
    val gameCustomerMap =
      hasStopPlaying.map((_._1)).map(c => c.getGameOrElse.get.id -> c).toMap
    val gameMap = games.map(g => g.id -> g).toMap
    val gameLeft = gameMap.keySet.intersect(gameCustomerMap.keySet)
    val (gameToUnlock, gameUnchanged) = gameMap.keySet.toList
      .map(id => (gameMap(id), gameCustomerMap.get(id)))
      .partition { case (game, cust) => cust.isDefined }
    val updatedGame =
      gameToUnlock.map((g, c) => g.unlock(c.get.id)).map(r => r.option().get)
    updatedGame ++ gameUnchanged.map((g, _) => g)

  private def groupForChangeOfState[P <: CustomerState[P] & Entity](
      before: Seq[P],
      post: Seq[P]
  ): (List[(P, P)], List[(P, P)], Set[String]) =
    val beforeMap = before.map(p => p.id -> p).toMap
    val postMap = post.map(p => p.id -> p).toMap
    val remained = beforeMap.keySet.intersect(postMap.keySet)
    val (hasStopPlaying, unchangedState) = remained.toList
      .map(id => (beforeMap(id), postMap(id)))
      .partition { case (oldState, newState) =>
        oldState.isPlaying != newState.isPlaying
      }
    (hasStopPlaying, unchangedState, remained)
