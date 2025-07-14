package model.entities.customers

import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.games._

trait HasBetStrategy[T <: HasBetStrategy[T] & Bankroll[T] & CustomerState[T]]:
  this: T =>
  val betStrategy: BettingStrategy[T]

  def placeBet(): Bet = betStrategy.placeBet(using this)

  def changeBetStrategy(newStrat: BettingStrategy[T]): T =
    changedBetStrategy(newStrat)

  protected def changedBetStrategy(newStrat: BettingStrategy[T]): T

trait BettingStrategy[A]:
  val betAmount: Double
  require(
    betAmount >= 0,
    s"Bet amount must be positive, instead is $betAmount"
  )

  def placeBet(using ctx: A): Bet
  def updateAfter(result: BetResult): BettingStrategy[A]

case class FlatBetting[A <: Bankroll[A] & CustomerState[A]](
    betAmount: Double,
    option: List[Int]
) extends BettingStrategy[A]:
  def placeBet(using ctx: A): Bet =
    require(
      betAmount <= ctx.bankroll,
      s"Bet amount must be equal or less of the total bankroll, instead is $betAmount when the bankroll is ${ctx.bankroll}"
    )
    require(
      ctx.customerState != Idle,
      "Bet should be placed only if the customer is playing a game"
    )
    ctx.customerState match
      case Playing(game) =>
        game.gameType match
          case SlotMachine => SlotBet(betAmount)
          case Roulette    => RouletteBet(betAmount, option)
          case Blackjack   => BlackJackBet(betAmount, option.head)
          case _           => ???

  def updateAfter(result: BetResult): FlatBetting[A] = this

object FlatBetting:

  def apply[A <: Bankroll[A] & CustomerState[A]](
      betAmount: Double,
      option: Int
  ): FlatBetting[A] =
    new FlatBetting[A](betAmount, List(option))

  def apply[A <: Bankroll[A] & CustomerState[A]](
      betAmount: Double,
      options: List[Int]
  ): FlatBetting[A] =
    new FlatBetting[A](betAmount, options)

  def apply[A <: Bankroll[A] & CustomerState[A]](
      betAmount: Double
  ): FlatBetting[A] =
    new FlatBetting[A](betAmount, List.empty)

case class Martingale[A <: Bankroll[A] & CustomerState[A]](
    betAmount: Double,
    lossStreak: Int = 0,
    option: List[Int]
) extends BettingStrategy[A]:

  def placeBet(using ctx: A): Bet =
    val bet = nextBet()
    ctx.customerState match
      case Playing(game) =>
        game.gameType match
          case Roulette  => RouletteBet(betAmount, option)
          case Blackjack => BlackJackBet(betAmount, option.head)
          case _         => ???

  def updateAfter(result: BetResult): Martingale[A] =
    if result.isFailure then
      this.copy(betAmount = nextBet(), lossStreak = lossStreak + 1)
    else copy(lossStreak = 0)

  def nextBet(): Double =
    betAmount * math.pow(2, lossStreak)

object Martingale:

  def apply[A <: Bankroll[A] & CustomerState[A]](
      betAmount: Double,
      option: Int
  ): Martingale[A] =
    Martingale(betAmount, 0, List(option))

  def apply[A <: Bankroll[A] & CustomerState[A]](
      betAmount: Double,
      options: List[Int]
  ): Martingale[A] =
    Martingale(betAmount, 0, options)

  def apply[A <: Bankroll[A] & CustomerState[A]](
      betAmount: Double,
      option: Int,
      lossStreak: Int
  ): Martingale[A] =
    new Martingale[A](betAmount, lossStreak, List(option))

  def apply[A <: Bankroll[A] & CustomerState[A]](
      betAmount: Double,
      options: List[Int],
      lossStreak: Int
  ): Martingale[A] =
    new Martingale[A](betAmount, lossStreak, options)

//case class KellyStrategy(p: Double, b: Double) extends BettingStrategy:
//
//  def placeBet(ctx: Customer) =
//    val fraction = ((b * p) - (1 - p)) / b
//    val amt = (ctx.bankroll * fraction).max(1.0)
//    Bet(amt, "default")
//  def updateAfter(result: BetResult) = this
//
//case class ReactiveRandomStrategy(base: Double, min: Double, max: Double) extends BettingStrategy:
//
//  def placeBet(ctx: Customer) = BetDecision(base, "random")
//
//  def updateAfter(result: GameResult) =
//    val newBase = if result.netGain < 0 then (base - 1).max(min) else (base + 1).min(max)
//    copy(base = newBase)
