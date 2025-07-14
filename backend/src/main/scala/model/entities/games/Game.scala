package model.entities.games

import scala.util.Random

import model.entities.Entity
import model.entities.games.dsl.use
import utils.Result
import utils.Result.Failure
import utils.Result.Success
import utils.Vector2D

trait GameType
object SlotMachine extends GameType
object Roulette extends GameType
object Blackjack extends GameType

trait Game(
    val id: String,
    val position: Vector2D,
    val gameState: GameState,
    val gameHistory: GameHistory
) extends Entity:

  def gameType: GameType
  def updateHistory(gain: Double): Game
  def lock(id: String): Result[Game, Game]
  def unlock(id: String): Result[Game, Game]
  def play[B <: Bet](bet: B): Result[BetResult, String]

  def bankroll: Double = this.gameHistory.overallGains

case class RouletteGame(
    override val id: String,
    override val position: Vector2D,
    override val gameState: GameState,
    override val gameHistory: GameHistory
) extends Game(id, position, gameState, gameHistory):
  override def gameType: GameType = Roulette

  override def lock(id: String): Result[Game, Game] =
    gameState.addPlayer(id) match
      case Success(newGameState) =>
        Success(this.copy(gameState = newGameState))
      case _ => Failure(this)

  override def unlock(id: String): Result[Game, Game] =
    gameState.removePlayer(id) match
      case Success(newGameState) =>
        Success(this.copy(gameState = newGameState))
      case _ => Failure(this)

  override def play[B <: Bet](bet: B): Result[BetResult, String] = bet match
    case b: RouletteBet =>
      val strategy =
        use(RouletteStrategy) bet b.amount on b.targets when true
      Success(strategy.use())
    case _ =>
      Failure("Applied a bet different from the RouletteBet to the Roulette!")

  override def updateHistory(gain: Double): Game =
    this.copy(gameHistory = this.gameHistory.update(gain))

case class SlotMachineGame(
    override val id: String,
    override val position: Vector2D,
    override val gameState: GameState,
    override val gameHistory: GameHistory
) extends Game(id, position, gameState, gameHistory):
  override def gameType: GameType = SlotMachine

  override def lock(id: String): Result[Game, Game] =
    gameState.addPlayer(id) match
      case Success(newGameState) =>
        Success(this.copy(gameState = newGameState))
      case _ => Failure(this)

  override def unlock(id: String): Result[Game, Game] =
    gameState.removePlayer(id) match
      case Success(newGameState) =>
        Success(this.copy(gameState = newGameState))
      case _ => Failure(this)

  override def play[B <: Bet](bet: B): Result[BetResult, String] = bet match
    case b: SlotBet =>
      val strategy =
        use(RouletteStrategy) bet b.amount when true
      Success(strategy.use())
    case _ =>
      Failure("Applied a bet different from the SlotBet to the Slot machine!")

  override def updateHistory(gain: Double): Game =
    this.copy(gameHistory = this.gameHistory.update(gain))

case class BlackJackGame(
    override val id: String,
    override val position: Vector2D,
    override val gameState: GameState,
    override val gameHistory: GameHistory
) extends Game(id, position, gameState, gameHistory):
  override def gameType: GameType = Blackjack

  override def lock(id: String): Result[Game, Game] =
    gameState.addPlayer(id) match
      case Success(newGameState) =>
        Success(this.copy(gameState = newGameState))
      case _ => Failure(this)

  override def unlock(id: String): Result[Game, Game] =
    gameState.removePlayer(id) match
      case Success(newGameState) =>
        Success(this.copy(gameState = newGameState))
      case _ => Failure(this)

  override def play[B <: Bet](bet: B): Result[BetResult, String] = bet match
    case b: BlackJackBet =>
      val strategy =
        use(BlackJackStrategy) bet b.amount accept b.minimumValue when true
      Success(strategy.use())
    case _ =>
      Failure("Applied a bet different from the BlackJackBet to the blackjack!")

  override def updateHistory(gain: Double): Game =
    this.copy(gameHistory = this.gameHistory.update(gain))

object GameBuilder:
  def roulette(position: Vector2D): RouletteGame =
    RouletteGame(
      Random.nextString(10),
      position,
      GameState(0, 6, List.empty),
      GameHistory(List.empty)
    )

  def slot(position: Vector2D): SlotMachineGame =
    SlotMachineGame(
      Random.nextString(10),
      position,
      GameState(0, 1, List.empty),
      GameHistory(List.empty)
    )

  def blackjack(position: Vector2D): BlackJackGame =
    BlackJackGame(
      Random.nextString(10),
      position,
      GameState(0, 7, List.empty),
      GameHistory(List.empty)
    )
