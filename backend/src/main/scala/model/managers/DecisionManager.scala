package model.managers

import scala.util.Random

import model.entities.ChangingFavouriteGamePlayer
import model.entities.Entity
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

/** Manages the decision-making process for customer entities within the casino
  * simulation.
  *
  * This manager utilizes a configurable Decision Tree to determine customer
  * actions based on their current state, risk profile, and game outcomes. It
  * aims to provide realistic and complex AI behavior.
  *
  * @param games
  *   A list of all available games in the casino, used to access game states.
  * @tparam A
  *   The type of customer entity this manager processes. It must possess
  *   Bankroll, BoredomFrustration, CustomerState, HasBetStrategy, Entity, and
  *   StatusProfile capabilities.
  */
case class DecisionManager[
    A <: Bankroll[A] & BoredomFrustration[A] & CustomerState[A] &
      HasBetStrategy[A] & Entity & StatusProfile
](games: List[Game])
    extends BaseManager[Seq[A]]: // Manages a sequence of customers
  private val gameList = games.map(_.gameType).distinct

  /** Defines limits for take-profit (tp) and stop-loss (sl) thresholds.
    * @param tp
    *   Take-profit ratio (e.g., 2.0 means 200% of starting bankroll).
    * @param sl
    *   Stop-loss ratio (e.g., 0.3 means 30% of starting bankroll remaining).
    */
  private case class Limits(tp: Double, sl: Double)

  /** Defines modifiers for various customer profiles, affecting decision
    * thresholds.
    * @param limits
    *   Specific take-profit and stop-loss limits for the profile.
    * @param bMod
    *   Boredom modifier (multiplier for boredom threshold).
    * @param fMod
    *   Frustration modifier (multiplier for frustration threshold).
    */
  private case class Modifiers(limits: Limits, bMod: Double, fMod: Double)

  /** Object containing predefined modifiers for different risk profiles. These
    * values can be easily adjusted to tune customer behavior.
    */
  private object ProfileModifiers:
    val modifiers: Map[RiskProfile, Modifiers] = Map(
      RiskProfile.VIP -> Modifiers(Limits(tp = 3.0, sl = 0.3), 1.30, 0.80),
      RiskProfile.Regular -> Modifiers(Limits(2.5, 0.3), 1.0, 1.0),
      RiskProfile.Casual -> Modifiers(Limits(2.0, 0.5), 1.40, 1.30),
      RiskProfile.Impulsive -> Modifiers(Limits(5.0, 0.1), 0.70, 1.5)
    )

  /** Defines a single rule for switching betting strategies or influencing
    * customer decisions. These rules are evaluated by the Decision Tree.
    *
    * @param profile
    *   The RiskProfile to which this rule applies.
    * @param game
    *   The specific GameType to which this rule applies.
    * @param strategy
    *   The current BetStratType of the customer for this rule to be active.
    * @param trigger
    *   A TriggerDSL condition that must be met for the rule to activate.
    * @param nextStrategy
    *   The BetStratType to switch to if the rule activates.
    * @param betPercentage
    *   The percentage of the customer's bankroll to use for the new bet amount.
    */
  case class SwitchRule(
      profile: RiskProfile,
      game: GameType,
      strategy: BetStratType,
      trigger: Trigger[A],
      nextStrategy: BetStratType,
      betPercentage: Double
  )

  /** Object containing the default predefined set of `SwitchRule`s. This is the
    * base configuration for customer decision logic.
    */
  object DefaultConfig:
    val switchRules: List[SwitchRule] = List(
      // VIP Rules
      SwitchRule(VIP, Blackjack, Martingale, Losses(3), OscarGrind, 0.05),
      SwitchRule(VIP, Roulette, Martingale, Losses(4), OscarGrind, 0.05),
      SwitchRule(
        VIP,
        SlotMachine,
        FlatBet,
        FrustAbove(50) || BrRatioBelow(0.5),
        FlatBet,
        0.015
      ),
      SwitchRule(VIP, SlotMachine, Martingale, Always, FlatBet, 0.03),
      SwitchRule(VIP, SlotMachine, OscarGrind, Always, FlatBet, 0.03),
      SwitchRule(VIP, Blackjack, FlatBet, Always, Martingale, 0.03),
      SwitchRule(VIP, Roulette, FlatBet, Always, Martingale, 0.03),
      // Regular Rules
      SwitchRule(
        Regular,
        Blackjack,
        OscarGrind,
        BrRatioAbove(1.3),
        Martingale,
        0.015
      ),
      SwitchRule(Regular, Blackjack, Martingale, Losses(3), OscarGrind, 0.02),
      SwitchRule(
        Regular,
        Roulette,
        OscarGrind,
        BrRatioAbove(1.3),
        Martingale,
        0.015
      ),
      SwitchRule(Regular, Roulette, Martingale, Losses(3), OscarGrind, 0.02),
      SwitchRule(
        Regular,
        SlotMachine,
        FlatBet,
        FrustAbove(60) || BrRatioBelow(0.5),
        FlatBet,
        0.01
      ),
      SwitchRule(Regular, SlotMachine, Martingale, Always, FlatBet, 0.03),
      SwitchRule(Regular, SlotMachine, OscarGrind, Always, FlatBet, 0.03),
      SwitchRule(Regular, Blackjack, FlatBet, Always, OscarGrind, 0.02),
      SwitchRule(Regular, Roulette, FlatBet, Always, OscarGrind, 0.02),
      // Casual Rules
      SwitchRule(
        Casual,
        SlotMachine,
        FlatBet,
        FrustAbove(50) || BrRatioBelow(0.7),
        FlatBet,
        0.015
      ),
      SwitchRule(Casual, SlotMachine, Martingale, Always, FlatBet, 0.03),
      SwitchRule(Casual, SlotMachine, OscarGrind, Always, FlatBet, 0.03),
      SwitchRule(Casual, Blackjack, FlatBet, Always, OscarGrind, 0.03),
      SwitchRule(Casual, Roulette, FlatBet, Always, FlatBet, 0.03),
      // Impulsive Rules
      SwitchRule(Impulsive, Blackjack, Martingale, Losses(3), OscarGrind, 0.10),
      SwitchRule(Impulsive, Roulette, Martingale, Losses(3), FlatBet, 0.07),
      SwitchRule(
        Impulsive,
        Roulette,
        FlatBet,
        BrRatioAbove(1),
        Martingale,
        0.03
      ),
      SwitchRule(
        Impulsive,
        SlotMachine,
        FlatBet,
        FrustAbove(50),
        FlatBet,
        0.02
      ),
      SwitchRule(Impulsive, SlotMachine, Martingale, Always, FlatBet, 0.03),
      SwitchRule(Impulsive, Blackjack, FlatBet, Always, Martingale, 0.02),
      SwitchRule(Impulsive, SlotMachine, OscarGrind, Always, FlatBet, 0.03),
      SwitchRule(Impulsive, Roulette, FlatBet, Always, Martingale, 0.03)
    )

  /** Object responsible for loading the configuration rules. Currently loads
    * from DefaultConfig but can be extended to load from external sources.
    */
  object ConfigLoader:
    def load(): List[SwitchRule] = DefaultConfig.switchRules

  /** Lazily evaluated map of rules, grouped by RiskProfile for efficient
    * lookup.
    */
  lazy val rulesByProfile: Map[RiskProfile, List[SwitchRule]] =
    ConfigLoader.load().groupBy(_.profile)

  /** Sealed trait defining the possible decisions a customer can make.
    */
  sealed trait CustomerDecision

  /** Decision: Continue playing the current game. */
  case class ContinuePlaying() extends CustomerDecision

  /** Decision: Stop playing the current game (transition to Idle). */
  case class StopPlaying() extends CustomerDecision

  /** Decision: Change the current betting strategy to a new one. */
  case class ChangeStrategy(newStrategy: BettingStrategy[A])
      extends CustomerDecision

  /** Decision: Stay idle in the casino (e.g., wait for a game). */
  case class Stay() extends CustomerDecision

  /** Decision: Leave the casino entirely. */
  case class LeaveCasino() extends CustomerDecision

  /** Decision: Wait for a game ( when a game is not yet ready to play). */
  case class WaitForGame() extends CustomerDecision

  /** Updates a sequence of customer entities based on their individual
    * decisions. For each customer, a decision tree is evaluated, and the
    * customer's state is updated accordingly. This method returns a new
    * sequence of customers, potentially with some customers filtered out if
    * they decide to leave the casino.
    *
    * @param customers
    *   The sequence of customer entities to update.
    * @return
    *   A new sequence of updated customer entities.
    */
  override def update(customers: Seq[A]): Seq[A] =
    val tree = buildDecisionTree // Build the decision tree dynamically
    customers.flatMap { c =>
      val mod = ProfileModifiers.modifiers(
        c.riskProfile
      ) // Get profile-specific modifiers
      val decision =
        tree.eval(c) // Evaluate the customer's decision using the tree

      decision match
        case ContinuePlaying() =>
          Some(updateInGameBehaviours(c, mod).updateBoredom(5.0 * mod.bMod))
        case StopPlaying() =>
          Some(c.changeState(Idle).updateFrustration(-20.0 * (2 - mod.fMod)))
        case ChangeStrategy(s) =>
          Some(c.changeBetStrategy(s).updateBoredom(-10.0 * (2 - mod.bMod)))
        case WaitForGame() => Some(getNewGameBet(c))
        case Stay()        => Some(c)
        case LeaveCasino() =>
          None // Customers deciding to leave are filtered out
    }

  /** Determines an initial or new betting strategy for a customer, typically
    * when they are starting a game or need to define a bet where no specific
    * rule applies. It prioritizes rules defined in `rulesByProfile`.
    *
    * @param c
    *   The customer entity.
    * @return
    *   The customer entity with a potentially changed betting strategy.
    */
  private def getNewGameBet(c: A): A =
    val rules = rulesByProfile(c.riskProfile)
    rules
      .collectFirst { // Find the first applicable rule
        case rule
            if rule.game == c.getGameOrElse.get.gameType && rule.strategy == c.betStrategy.betType && rule.trigger
              .eval(c) =>
          c.changeBetStrategy(betDefiner(rule, c))
      }
      .getOrElse( // If no rule matches, apply a default FlatBetting strategy
        c.changeBetStrategy(FlatBetting(c.bankroll * 0.01, defaultRedBet))
      )

  /** Updates a customer's in-game behavior attributes (frustration, strategy
    * internal state) based on the outcome of the last game round.
    *
    * @param c
    *   The customer entity.
    * @param mod
    *   Profile-specific modifiers for calculations.
    * @return
    *   The updated customer entity.
    */
  private def updateInGameBehaviours(c: A, mod: Modifiers): A =
    // Find the game the customer was playing and its last round result
    val updatedGame = games.find(_.id == c.getGameOrElse.get.id).get
    val lastRound = updatedGame.getLastRoundResult
    lastRound.find(_.getCustomerWhichPlayed == c.id) match
      case Some(g) => // If the customer played the last round
        if g.getMoneyGain > 0 then // Customer won
          c.updateFrustration(
            (5 / c.bankrollRatio
              .max(0.7)
              .min(2.0)) * mod.fMod // Adjust frustration based on win
          ).updateAfter(
            -g.getMoneyGain
          ) // Update betting strategy with negative gain (loss for strategy context)
        else // Customer lost or pushed
          c.updateFrustration(
            (-3 / c.bankrollRatio
              .max(0.7)
              .min(2.0)) * (2 - mod.fMod) // Adjust frustration based on loss
          ).updateAfter(
            -g.getMoneyGain
          ) // Update betting strategy with negative gain
      case _ => c // Customer did not play the last round, return as is

  /** Builds the main decision tree for customer actions. The tree's root
    * branches based on whether the customer is currently playing.
    *
    * @return
    *   The root of the DecisionTree.
    */
  private def buildDecisionTree: DecisionTree[A, CustomerDecision] =
    DecisionNode[A, CustomerDecision](
      predicate = _.isPlaying, // Check if the customer's state is Playing
      trueBranch = gameNode, // If playing, go to game-specific decisions
      falseBranch = leaveStayNode // If idle, decide whether to leave or stay
    )

  /** Subtree for decisions when a customer is thought to be playing. Checks if
    * there's actually a recent game round result for the customer.
    *
    * @return
    *   A DecisionTree representing game-related checks.
    */
  private def gameNode: DecisionTree[A, CustomerDecision] =
    def checkIfPlaying(c: A): Boolean =
      // Check if the customer's game actually has a last round result for them
      val updatedGame = games.find(_.id == c.getGameOrElse.get.id).get
      val lastRound = updatedGame.getLastRoundResult
      lastRound.nonEmpty

    DecisionNode[A, CustomerDecision](
      predicate = c => checkIfPlaying(c),
      trueBranch =
        profileNode, // If actually played, proceed to profile-specific decisions
      falseBranch = Leaf[A, CustomerDecision](c =>
        WaitForGame()
      ) // If not played, wait for game
    )

  /** Subtree that branches decisions based on the customer's RiskProfile. Uses
    * a MultiNode to dispatch to different decision paths for each profile.
    *
    * @return
    *   A DecisionTree branching by customer risk profile.
    */
  private def profileNode: DecisionTree[A, CustomerDecision] =
    MultiNode[A, RiskProfile, CustomerDecision](
      keyOf = _.riskProfile, // Key is the customer's risk profile
      branches =
        RiskProfile.values
          .map(p => p -> stopContinueNode(p))
          .toMap, // Map each profile to its decision sub-tree
      default = Leaf[A, CustomerDecision](c =>
        StopPlaying()
      ) // Default action if profile somehow not mapped
    )

  /** Subtree for idle customers, deciding whether they should leave the casino
    * or stay. Decision is based on boredom, frustration, and bankroll limits.
    *
    * @return
    *   A DecisionTree for idle customer's leave/stay decision.
    */
  private def leaveStayNode: DecisionTree[A, CustomerDecision] =
    def leaveRequirements(c: A): Boolean =
      val mod = ProfileModifiers.modifiers(c.riskProfile)
      // Compound trigger checking if boredom/frustration/bankroll limits are met for leaving
      val trigger: Trigger[A] = BoredomAbove(
        (80 * mod.bMod).min(98.0)
      ) || FrustAbove((80 * mod.fMod).min(85.0))
        || BrRatioAbove(mod.limits.tp) || BrRatioBelow(mod.limits.sl)
      trigger.eval(c)
    DecisionNode[A, CustomerDecision](
      predicate = c => leaveRequirements(c),
      trueBranch = Leaf[A, CustomerDecision](c =>
        LeaveCasino()
      ), // If conditions met, leave
      falseBranch = Leaf[A, CustomerDecision](c => Stay()) // Otherwise, stay
    )

  /** Subtree for in-game customers, deciding whether to stop playing or
    * continue. Decision is based on current bet status, boredom, frustration,
    * and bankroll limits.
    *
    * @param profile
    *   The RiskProfile of the customer (used to retrieve modifiers).
    * @param bThreshold
    *   Base boredom threshold.
    * @param fThreshold
    *   Base frustration threshold.
    * @return
    *   A DecisionTree for in-game customer's stop/continue decision.
    */
  private def stopContinueNode(
      profile: RiskProfile,
      bThreshold: Double = 60,
      fThreshold: Double = 50
  ): DecisionTree[A, CustomerDecision] =
    def stopPlayingRequirements(c: A): Boolean =
      val mod = ProfileModifiers.modifiers(profile)
      // Simulate applying game outcome to strategy temporarily to check next bet amount
      val betAmount = updateInGameBehaviours(c, mod).betStrategy.betAmount
      // Compound trigger checking if boredom/frustration/bankroll limits or impossible bet are met for stopping
      val trigger: Trigger[A] =
        BoredomAbove(bThreshold * mod.bMod) || FrustAbove(fThreshold * mod.fMod)
          || BrRatioAbove(mod.limits.tp) || BrRatioBelow(mod.limits.sl)
      betAmount > c.bankroll - 1 || trigger.eval(
        c
      ) // Stop if next bet is impossible or trigger activates

    DecisionNode[A, CustomerDecision](
      predicate = c => stopPlayingRequirements(c),
      trueBranch = Leaf[A, CustomerDecision](c =>
        StopPlaying()
      ), // If conditions met, stop playing
      falseBranch = strategySwitchNode(
        profile
      ) // Otherwise, proceed to strategy switching logic
    )

  /** Subtree for determining if a customer should switch betting strategies.
    * This is a Leaf node that evaluates defined `SwitchRule`s for the
    * customer's profile.
    *
    * @param profile
    *   The RiskProfile of the customer.
    * @return
    *   A Leaf DecisionTree that either suggests a strategy change or continues
    *   playing.
    */
  private def strategySwitchNode(
      profile: RiskProfile
  ): DecisionTree[A, CustomerDecision] =
    val rules = rulesByProfile.getOrElse(profile, Nil)
    Leaf[A, CustomerDecision] { c =>
      rules
        .collectFirst { // Find the first rule that applies
          case rule
              if rule.game == c.getGameOrElse.get.gameType && rule.strategy == c.betStrategy.betType && rule.trigger
                .eval(c) =>
            ChangeStrategy(
              betDefiner(rule, c)
            ) // If rule applies, suggest changing strategy
        }
        .getOrElse(ContinuePlaying()) // If no rule applies, continue playing
    }

  /** Helper function to instantiate a new BettingStrategy based on a
    * `SwitchRule`. Calculates the new bet amount as a percentage of the
    * customer's bankroll.
    *
    * @param rule
    *   The SwitchRule specifying the next strategy type and bet percentage.
    * @param c
    *   The customer entity.
    * @return
    *   A new instance of the appropriate BettingStrategy.
    */
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

/** An object responsible for updating the simulation environment after customer
  * decisions have been made.
  *
  * This component handles "side effects" such as updating customer positions,
  * changing their favorite games, and unlocking casino games, maintaining a
  * functional separation from the core decision-making logic.
  */
object PostDecisionUpdater:
  /** Updates the physical position and favorite game of customers who have
    * stopped playing. Customers who transition from 'Playing' to 'Idle' are
    * moved back to their previous position and assigned a new random favorite
    * game.
    *
    * @param before
    *   The sequence of customer entities before decision processing.
    * @param post
    *   The sequence of customer entities after decision processing.
    * @tparam P
    *   The type of customer entity, which must have MovableWithPrevious,
    *   CustomerState, ChangingFavouriteGamePlayer, and Entity capabilities.
    * @return
    *   A list of updated customer entities with their new positions and
    *   favorite games.
    */
  def updatePosition[
      P <: MovableWithPrevious[P] & CustomerState[P] &
        ChangingFavouriteGamePlayer[P] & Entity
  ](before: Seq[P], post: Seq[P]): List[P] =
    val (hasStopPlaying, unchangedState, remained) =
      groupForChangeOfState[P](
        before,
        post
      ) // Identify customers who stopped playing

    // For customers who stopped playing, update their position and assign a new favorite game
    val changePosition = hasStopPlaying.map { case (oldP, newP) =>
      newP
        .withPosition(
          oldP.previousPosition.get
        ) // Move back to previous position
        .withDirection(-newP.direction) // Reverse direction
        .withFavouriteGame(
          Random
            .shuffle(gameTypesPresent.filter(_ != newP.favouriteGame))
            .head // Assign new random favorite game
        )
    }.toList // Convert to List after map

    val unchanged =
      unchangedState
        .map(_._2)
        .toList // Customers whose playing state didn't change
    changePosition ++ unchanged // Combine updated and unchanged customers

  /** Updates the state of casino games, specifically unlocking games that were
    * previously occupied by customers who have now stopped playing.
    *
    * @param before
    *   The sequence of customer entities before decision processing.
    * @param post
    *   The sequence of customer entities after decision processing.
    * @param games
    *   The list of all available Game entities in the casino.
    * @tparam P
    *   The type of customer entity, which must have CustomerState and Entity
    *   capabilities.
    * @return
    *   A list of updated Game entities.
    */
  def updateGames[P <: CustomerState[P] & Entity](
      before: Seq[P],
      post: Seq[P],
      games: List[Game]
  ): List[Game] =
    val (hasStopPlaying, unchangedState, remained) =
      groupForChangeOfState(
        before,
        post
      ) // Identify customers who stopped playing a game

    val gameCustomerMap =
      hasStopPlaying
        .map((_._1))
        .map(c => c.getGameOrElse.get.id -> c)
        .toMap // Map game IDs to customers who left them
    val gameMap =
      games.map(g => g.id -> g).toMap // Map game IDs to Game objects

    // Partition games into those that need unlocking and those that remain unchanged
    val (gameToUnlock, gameUnchanged) = gameMap.keySet.toList
      .map(id => (gameMap(id), gameCustomerMap.get(id)))
      .partition { case (game, custOption) => custOption.isDefined }

    // Unlock games and convert to list
    val updatedGame =
      gameToUnlock
        .map((g, c) => g.unlock(c.get.id))
        .map(r => r.option().get)
        .toList

    // Combine updated (unlocked) games with games that were not affected
    updatedGame ++ gameUnchanged.map((g, _) => g).toList

  /** A private helper method to group customers based on changes in their
    * playing state. It compares customer states before and after decision
    * processing.
    *
    * @param before
    *   The sequence of customer entities before processing.
    * @param post
    *   The sequence of customer entities after processing.
    * @tparam P
    *   The type of customer entity.
    * @return
    *   A tuple containing:
    *   1. A list of (old customer state, new customer state) for customers who
    *      stopped playing. 2. A list of (old customer state, new customer
    *      state) for customers whose playing state remained unchanged. 3. A set
    *      of IDs for customers who remained in the simulation.
    */
  private def groupForChangeOfState[P <: CustomerState[P] & Entity](
      before: Seq[P],
      post: Seq[P]
  ): (List[(P, P)], List[(P, P)], Set[String]) =
    val beforeMap = before.map(p => p.id -> p).toMap
    val postMap = post.map(p => p.id -> p).toMap
    val remained = beforeMap.keySet.intersect(
      postMap.keySet
    ) // IDs of customers still in simulation

    // Partition remaining customers into those who stopped playing and those whose state is unchanged
    val (hasStopPlaying, unchangedState) = remained.toList
      .map(id => (beforeMap(id), postMap(id)))
      .partition { case (oldState, newState) =>
        oldState.isPlaying != newState.isPlaying // Check if 'isPlaying' state has changed
      }
    (hasStopPlaying, unchangedState, remained)
