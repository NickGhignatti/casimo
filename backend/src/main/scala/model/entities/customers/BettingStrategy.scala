package model.entities.customers

import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.games._

/** Defines a contract for entities that possess a betting strategy.
  *
  * This trait facilitates the integration of dynamic betting behaviors into
  * entities that manage their own bankroll and customer state. It ensures that
  * such entities can place bets, update their strategy based on outcomes, and
  * change strategies.
  *
  * @tparam T
  *   The concrete type of the entity that extends this trait. It must also have
  *   Bankroll, CustomerState, and be able to provide a new instance with a
  *   changed betting strategy.
  */
trait HasBetStrategy[T <: HasBetStrategy[T] & Bankroll[T] & CustomerState[T]]:
  this: T => // Self-type annotation ensures that 'this' is of type T, allowing 'copy' methods etc.
  val betStrategy: BettingStrategy[T]

  /** Delegates the bet placement to the current betting strategy. The current
    * entity instance is passed as context to the strategy.
    * @return
    *   A Bet object representing the placed bet.
    */
  def placeBet(): Bet = betStrategy.placeBet(this)

  /** Updates the entity's betting strategy based on the outcome of a game
    * round. The current strategy's `updateAfter` method is called, and the
    * entity is returned with the updated strategy (maintaining immutability).
    *
    * @param result
    *   The outcome of the game round (e.g., money won/lost, 0 for push).
    * @return
    *   A new instance of the entity with the updated betting strategy.
    */
  def updateAfter(result: Double): T =
    withBetStrategy(betStrategy.updateAfter(this, result))

  /** Changes the entity's current betting strategy to a new one.
    * @param newStrat
    *   The new BettingStrategy to adopt.
    * @return
    *   A new instance of the entity with the changed betting strategy.
    */
  def changeBetStrategy(newStrat: BettingStrategy[T]): T =
    withBetStrategy(newStrat)

  /** Abstract method to be implemented by concrete entities. It provides a way
    * to return a new instance of the entity with an updated betting strategy,
    * typically implemented using case class's `copy` method.
    *
    * @param newStrat
    *   The new BettingStrategy to be set.
    * @return
    *   A new instance of the entity with the new strategy.
    */
  def withBetStrategy(newStrat: BettingStrategy[T]): T

/** Defines the types of betting strategies.
  */
sealed trait BetStratType

/** Represents a Flat Betting strategy type. */
object FlatBet extends BetStratType

/** Represents a Martingale strategy type. */
object Martingale extends BetStratType

/** Represents an Oscar Grind strategy type. */
object OscarGrind extends BetStratType

/** Defines the common interface and properties for all betting strategies.
  *
  * Betting strategies are responsible for determining the bet amount and
  * options, and for updating their internal state based on game outcomes.
  *
  * @tparam A
  *   The type of the entity (customer) that uses this strategy. It must have
  *   Bankroll and CustomerState capabilities.
  */
trait BettingStrategy[A <: Bankroll[A] & CustomerState[A]]:
  val betAmount: Double // The current amount to bet in the next round
  val option: List[
    Int
  ] // Game-specific options for the bet (e.g., numbers for Roulette)

  require(
    betAmount >= 0,
    s"Bet amount must be positive, instead is $betAmount"
  )

  /** Returns the type of this betting strategy.
    * @return
    *   The BetStratType for this strategy.
    */
  def betType: BetStratType

  /** Generates a game-specific Bet object based on the strategy's current state
    * and the customer's context.
    *
    * @param ctx
    *   The customer entity's context.
    * @return
    *   A Bet object ready to be placed in a game.
    */
  def placeBet(ctx: A): Bet

  /** Updates the internal state of the betting strategy based on the previous
    * game round's result. This method typically returns a new instance of the
    * strategy with updated parameters (e.g., for progressive strategies like
    * Martingale).
    *
    * @param ctx
    *   The customer entity's context (can be used for contextual updates).
    * @param result
    *   The outcome of the game round (positive for win, negative for loss, 0
    *   for push).
    * @return
    *   A new instance of the BettingStrategy with its state updated.
    */
  def updateAfter(ctx: A, result: Double): BettingStrategy[A]

  /** Checks preconditions before placing a bet. Throws a `require` error if the
    * bet amount exceeds bankroll or if the customer is not in a Playing state.
    *
    * @param ctx
    *   The customer entity's context.
    */
  protected def checkRequirement(ctx: A): Unit =
    require(
      betAmount <= ctx.bankroll,
      s"Bet amount must be equal or less of the total bankroll, instead is $betAmount when the bankroll is ${ctx.bankroll}"
    )
    require(
      ctx.customerState != Idle,
      "Bet should be placed only if the customer is playing a game"
    )

/** Default options for a red bet in Roulette.
  */
def defaultRedBet: List[Int] =
  List(16, 1, 3, 5, 7, 9, 12, 14, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)

/** Implements a Flat Betting strategy. The bet amount remains constant
  * regardless of previous game outcomes.
  *
  * @param betAmount
  *   The fixed amount to bet.
  * @param option
  *   Game-specific options for the bet.
  * @tparam A
  *   The entity type using this strategy.
  */
case class FlatBetting[A <: Bankroll[A] & CustomerState[A]](
    betAmount: Double,
    option: List[Int]
) extends BettingStrategy[A]:
  override def betType: BetStratType = FlatBet

  /** Generates a game-specific bet for Flat Betting. Checks requirements and
    * creates the appropriate Bet type based on the game.
    *
    * @param ctx
    *   The customer entity's context.
    * @return
    *   A Bet object.
    * @throws MatchError
    *   if the customer state is not Playing or game type is unknown.
    */
  override def placeBet(ctx: A): Bet =
    checkRequirement(ctx)
    (ctx.customerState: @unchecked) match
      case Playing(game) =>
        game.gameType match
          case SlotMachine => SlotBet(betAmount)
          case Roulette    => RouletteBet(betAmount, option)
          case Blackjack   => BlackJackBet(betAmount, option.head)
          case _           => ??? // Placeholder for unhandled game types
  // case Idle => throw new MatchError("Wrong customer state") // This case is guarded by checkRequirement

  /** For Flat Betting, the strategy's state does not change after a game round.
    * Returns the current instance.
    * @param ctx
    *   The customer entity's context (unused in FlatBetting).
    * @param result
    *   The outcome of the game round (unused in FlatBetting).
    * @return
    *   This FlatBetting instance.
    */
  override def updateAfter(ctx: A, result: Double): FlatBetting[A] = this

/** Companion object for FlatBetting, providing convenient factory methods. */
object FlatBetting:
  /** Creates a FlatBetting strategy with a single option.
    * @param betAmount
    *   The fixed bet amount.
    * @param option
    *   The single game option.
    * @tparam A
    *   The entity type.
    * @return
    *   A new FlatBetting instance.
    */
  def apply[A <: Bankroll[A] & CustomerState[A]](
      betAmount: Double,
      option: Int
  ): FlatBetting[A] =
    new FlatBetting[A](betAmount, List(option))

  /** Creates a FlatBetting strategy with a list of options.
    * @param betAmount
    *   The fixed bet amount.
    * @param options
    *   The list of game options.
    * @tparam A
    *   The entity type.
    * @return
    *   A new FlatBetting instance.
    */
  def apply[A <: Bankroll[A] & CustomerState[A]](
      betAmount: Double,
      options: List[Int]
  ): FlatBetting[A] =
    new FlatBetting[A](betAmount, options)

  /** Creates a FlatBetting strategy with no specific options (e.g., for Slot
    * Machines).
    * @param betAmount
    *   The fixed bet amount.
    * @tparam A
    *   The entity type.
    * @return
    *   A new FlatBetting instance.
    */
  def apply[A <: Bankroll[A] & CustomerState[A]](
      betAmount: Double
  ): FlatBetting[A] =
    new FlatBetting[A](betAmount, List.empty)

/** Implements the Martingale betting strategy. The bet amount doubles after
  * each loss and resets to base after a win.
  *
  * @param baseBet
  *   The initial bet amount to which the strategy resets after a win.
  * @param betAmount
  *   The current bet amount for the next round.
  * @param lossStreak
  *   The current number of consecutive losses.
  * @param option
  *   Game-specific options for the bet.
  * @tparam A
  *   The entity type using this strategy.
  */
case class MartingaleStrat[A <: Bankroll[A] & CustomerState[A]](
    baseBet: Double,
    betAmount: Double,
    lossStreak: Int = 0,
    option: List[Int]
) extends BettingStrategy[A]:
  override def betType: BetStratType = Martingale

  /** Generates a game-specific bet for Martingale strategy. Currently supports
    * Roulette and Blackjack.
    *
    * @param ctx
    *   The customer entity's context.
    * @return
    *   A Bet object.
    * @throws MatchError
    *   if the customer state is not Playing or game type is unsupported.
    */
  override def placeBet(ctx: A): Bet =
    checkRequirement(ctx)
    (ctx.customerState: @unchecked) match
      case Playing(game) =>
        game.gameType match
          case Roulette  => RouletteBet(betAmount, option)
          case Blackjack => BlackJackBet(betAmount, option.head)
          case _         => ??? // Placeholder for unhandled game types

  /** Updates the Martingale strategy's state based on the game result. If
    * result is negative (loss), bet doubles and loss streak increments. If
    * result is positive (win), bet resets to base and loss streak resets. If
    * result is zero (push), state remains unchanged.
    *
    * @param ctx
    *   The customer entity's context.
    * @param result
    *   The outcome of the game round.
    * @return
    *   A new MartingaleStrat instance with updated state.
    */
  override def updateAfter(ctx: A, result: Double): MartingaleStrat[A] =
    if result < 0 then
      this.copy(betAmount = nextBet(), lossStreak = lossStreak + 1)
    else if result == 0 then this
    else copy(betAmount = baseBet, lossStreak = 0)

  /** Calculates the next bet amount based on the current loss streak for
    * Martingale.
    * @return
    *   The next bet amount.
    */
  def nextBet(): Double =
    baseBet * math.pow(2, lossStreak + 1)

/** Companion object for MartingaleStrat, providing convenient factory methods.
  */
object MartingaleStrat:
  /** Creates a Martingale strategy with a single option, starting with baseBet.
    * @param baseBet
    *   The initial bet amount.
    * @param option
    *   The single game option.
    * @tparam A
    *   The entity type.
    * @return
    *   A new MartingaleStrat instance.
    */
  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      option: Int
  ): MartingaleStrat[A] =
    MartingaleStrat(baseBet, baseBet, 0, List(option))

  /** Creates a Martingale strategy with a list of options, starting with
    * baseBet.
    * @param baseBet
    *   The initial bet amount.
    * @param options
    *   The list of game options.
    * @tparam A
    *   The entity type.
    * @return
    *   A new MartingaleStrat instance.
    */
  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      options: List[Int]
  ): MartingaleStrat[A] =
    MartingaleStrat(baseBet, baseBet, 0, options)

  // Additional apply methods for more specific initialization (e.g., restoring state)
  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      betAmount: Double,
      option: Int,
      lossStreak: Int
  ): MartingaleStrat[A] =
    new MartingaleStrat[A](baseBet, betAmount, lossStreak, List(option))

  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      betAmount: Double,
      options: List[Int],
      lossStreak: Int
  ): MartingaleStrat[A] =
    new MartingaleStrat[A](baseBet, betAmount, lossStreak, options)

/** Implements the Oscar's Grind betting strategy. Aims for a single unit profit
  * per cycle, adjusting bets based on wins and losses.
  *
  * @param baseBet
  *   The base unit bet amount.
  * @param betAmount
  *   The current bet amount for the next round.
  * @param startingBankroll
  *   The bankroll at the start of the current Oscar Grind cycle.
  * @param lossStreak
  *   The current number of consecutive losses (not always used directly in bet
  *   calculation for Oscar Grind).
  * @param option
  *   Game-specific options for the bet.
  * @tparam A
  *   The entity type using this strategy.
  */
case class OscarGrindStrat[A <: Bankroll[A] & CustomerState[A]](
    baseBet: Double,
    betAmount: Double,
    startingBankroll: Double,
    lossStreak: Int = 0,
    option: List[Int]
) extends BettingStrategy[A]:
  override def betType: BetStratType = OscarGrind

  /** Generates a game-specific bet for Oscar's Grind strategy. Currently
    * supports Roulette and Blackjack.
    *
    * @param ctx
    *   The customer entity's context.
    * @return
    *   A Bet object.
    * @throws MatchError
    *   if the customer state is not Playing or game type is unsupported.
    */
  override def placeBet(ctx: A): Bet =
    checkRequirement(ctx)
    (ctx.customerState: @unchecked) match
      case Playing(game) =>
        game.gameType match
          case Roulette  => RouletteBet(betAmount, option)
          case Blackjack => BlackJackBet(betAmount, option.head)
          case _         => ??? // Placeholder for unhandled game types

  /** Updates the Oscar's Grind strategy's state based on the game result.
    * Logic:
    *   - If current bankroll exceeds startingBankroll, cycle profit achieved:
    *     reset betAmount to baseBet, update startingBankroll.
    *   - If result is positive (win) and no profit goal reached: increase
    *     betAmount by baseBet.
    *   - If result is zero (push): state remains unchanged.
    *   - If result is negative (loss): increment lossStreak (betAmount remains
    *     constant).
    *
    * @param ctx
    *   The customer entity's context.
    * @param result
    *   The outcome of the game round.
    * @return
    *   A new OscarGrindStrat instance with updated state.
    */
  override def updateAfter(ctx: A, result: Double): OscarGrindStrat[A] =
    if ctx.bankroll > startingBankroll then
      this.copy(betAmount = baseBet, startingBankroll = ctx.bankroll)
    else if result > 0 then
      this.copy(betAmount = betAmount + baseBet, lossStreak = 0)
    else if result == 0 then this
    else this.copy(lossStreak = lossStreak + 1)

/** Companion object for OscarGrindStrat, providing convenient factory methods.
  */
object OscarGrindStrat:
  /** Creates an OscarGrind strategy, starting a new cycle.
    * @param baseBet
    *   The base unit bet amount.
    * @param bankroll
    *   The current bankroll, which becomes the starting bankroll for the cycle.
    * @param option
    *   The single game option.
    * @tparam A
    *   The entity type.
    * @return
    *   A new OscarGrindStrat instance.
    */
  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      bankroll: Double,
      option: Int
  ): OscarGrindStrat[A] =
    OscarGrindStrat(baseBet, baseBet, bankroll, 0, List(option))

  /** Creates an OscarGrind strategy, starting a new cycle.
    * @param baseBet
    *   The base unit bet amount.
    * @param bankroll
    *   The current bankroll, which becomes the starting bankroll for the cycle.
    * @param options
    *   The list of game options.
    * @tparam A
    *   The entity type.
    * @return
    *   A new OscarGrindStrat instance.
    */
  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      bankroll: Double,
      options: List[Int]
  ): OscarGrindStrat[A] =
    OscarGrindStrat(baseBet, baseBet, bankroll, 0, options)

  // Additional apply methods for more specific initialization (e.g., restoring state)
  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      bankroll: Double,
      option: Int,
      lossStreak: Int
  ): OscarGrindStrat[A] =
    OscarGrindStrat(baseBet, baseBet, bankroll, lossStreak, List(option))

  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      bankroll: Double,
      options: List[Int],
      lossStreak: Int
  ): OscarGrindStrat[A] =
    OscarGrindStrat(baseBet, baseBet, bankroll, lossStreak, options)
