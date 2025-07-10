package model.entities.customers

import model.entities.customers.CustState.{Idle, Playing}
import model.entities.games.*

trait BetStrategy[T <: BetStrategy[T] & Bankroll[T] & CustomerState[T]]:
  val betStrategy: BettingStrategy[T]

  def changeBetStrategy(newStrat: BettingStrategy[T]): T =
    changedBetStrategy(newStrat)

  protected def changedBetStrategy(newStrat: BettingStrategy[T]): T

trait BettingStrategy[A]:

  def placeBet(ctx: A): Bet
  def updateAfter(result: BetResult): BettingStrategy[A]

case class FlatBetting[A <: Bankroll[A] & CustomerState[A]](
    amount: Double,
    option: Int*
) extends BettingStrategy[A]:
  def placeBet(ctx: A): Bet =
    require(
      amount <= ctx.bankroll,
      s"Bet amount must be equal or less of the total bankroll, instead is $amount when the bankroll is ${ctx.bankroll}"
    )
    require(
      ctx.customerState != Idle,
      "Bet should be placed only if the customer is playing a game"
    )
    ctx.customerState match
      case Playing(game) =>
        game.gameType match
          case SlotMachine => SlotBet(amount)
          case Roulette    => RouletteBet(amount, option.toList)
          case Blackjack   => BlackJackBet(amount, option.head)
          case _           => ???

  def updateAfter(result: BetResult): FlatBetting[A] = this

//case class MartingaleStrategy(baseBet: Double, lossStreak: Int = 0) extends BettingStrategy:
//
//  def placeBet(ctx: Customer): Bet =
//    val bet = nextBet()
//    Bet(bet, "default")
//
//  def updateAfter(result: BetResult) =
//    if result.isFailure then copy(lossStreak = lossStreak + 1) else copy(lossStreak = 0)
//
//  def nextBet(): Double =
//    baseBet * math.pow(2, lossStreak)
//
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
