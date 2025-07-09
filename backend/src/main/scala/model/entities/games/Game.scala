package model.entities.games

import model.entities.Entity
import model.entities.games.dsl.use
import utils.Result
import utils.Result.{Failure, Success}
import utils.Vector2D

import scala.util.Random

trait GameType
object SlotMachine extends GameType
object Roulette extends GameType
object Blackjack extends GameType

trait Game(
    val id: String,
    val position: Vector2D,
    val gameState: GameState
) extends Entity:

  def gameType: GameType
  def lock(): Result[Game, Game]
  def unlock(): Result[Game, Game]
  def play[B <: Bet](bet: B): Result[BetResult, String]

case class RouletteGame(
    override val id: String,
    override val position: Vector2D,
    override val gameState: GameState
) extends Game(id, position, gameState):
  override def gameType: GameType = Roulette

  override def lock(): Result[Game, Game] =
    gameState.addPlayer() match
      case Success(newGameState) =>
        Success(this.copy(gameState = newGameState))
      case _ => Failure(this)

  override def unlock(): Result[Game, Game] =
    gameState.removePlayer() match
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

case class SlotMachineGame(
    override val id: String,
    override val position: Vector2D,
    override val gameState: GameState
) extends Game(id, position, gameState):
  override def gameType: GameType = SlotMachine

  override def lock(): Result[Game, Game] =
    gameState.addPlayer() match
      case Success(newGameState) =>
        Success(this.copy(gameState = newGameState))
      case _ => Failure(this)

  override def unlock(): Result[Game, Game] =
    gameState.removePlayer() match
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

case class BlackJackGame(
    override val id: String,
    override val position: Vector2D,
    override val gameState: GameState
) extends Game(id, position, gameState):
  override def gameType: GameType = Blackjack

  override def lock(): Result[Game, Game] =
    gameState.addPlayer() match
      case Success(newGameState) =>
        Success(this.copy(gameState = newGameState))
      case _ => Failure(this)

  override def unlock(): Result[Game, Game] =
    gameState.removePlayer() match
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

object GameBuilder:
  def roulette(position: Vector2D): RouletteGame =
    RouletteGame(Random.nextString(10), position, GameState(0, 6))

  def slot(position: Vector2D): SlotMachineGame =
    SlotMachineGame(Random.nextString(10), position, GameState(0, 1))

  def blackjack(position: Vector2D): BlackJackGame =
    BlackJackGame(Random.nextString(10), position, GameState(0, 7))
