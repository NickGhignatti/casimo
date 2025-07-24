package model.entities.games

import scala.util.Random

import model.entities.CollidableEntity
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
) extends CollidableEntity:

  def gameType: GameType
  def play[B <: Bet](bet: B): Result[BetResult, String]

  protected def withGameState(newGameState: GameState): Game
  protected def withGameHistory(newGameHistory: GameHistory): Game

  def lock(id: String): Result[Game, Game] =
    gameState.addPlayer(id) match
      case Success(newGameState) => Success(withGameState(newGameState))
      case Failure(_)            => Failure(this)

  def unlock(id: String): Result[Game, Game] =
    gameState.removePlayer(id) match
      case Success(newGameState) => Success(withGameState(newGameState))
      case Failure(_)            => Failure(this)

  def updateHistory(customerId: String, gain: Double): Game =
    withGameHistory(gameHistory.update(customerId, gain))

  def isFull: Boolean = gameState.isFull
  def bankroll: Double = gameHistory.overallGains
  def getLastRoundResult: List[Gain] =
    gameHistory.gains.takeRight(gameState.currentPlayers)

case class RouletteGame(
    override val id: String,
    override val position: Vector2D,
    override val gameState: GameState,
    override val gameHistory: GameHistory
) extends Game(id, position, gameState, gameHistory):

  override def gameType: GameType = Roulette

  override protected def withGameState(newGameState: GameState): Game =
    copy(gameState = newGameState)

  override protected def withGameHistory(newGameHistory: GameHistory): Game =
    copy(gameHistory = newGameHistory)

  override def play[B <: Bet](bet: B): Result[BetResult, String] =
    bet match
      case b: RouletteBet =>
        val strategy =
          use(RouletteStrategy).bet(b.amount).on(b.targets).when(true)
        Success(strategy.use())
      case _ =>
        Failure("Applied a bet different from the RouletteBet to the Roulette!")

  override val width: Double = 30.0
  override val height: Double = 30.0

case class SlotMachineGame(
    override val id: String,
    override val position: Vector2D,
    override val gameState: GameState,
    override val gameHistory: GameHistory
) extends Game(id, position, gameState, gameHistory):

  override def gameType: GameType = SlotMachine

  override protected def withGameState(newGameState: GameState): Game =
    copy(gameState = newGameState)

  override protected def withGameHistory(newGameHistory: GameHistory): Game =
    copy(gameHistory = newGameHistory)

  override def play[B <: Bet](bet: B): Result[BetResult, String] =
    bet match
      case b: SlotBet =>
        val strategy = use(SlotStrategy)
          .bet(b.amount)
          .when(true)
        Success(strategy.use())
      case _ =>
        Failure("Applied a bet different from the SlotBet to the Slot machine!")

  override val width: Double = 20.0
  override val height: Double = 20.0

case class BlackJackGame(
    override val id: String,
    override val position: Vector2D,
    override val gameState: GameState,
    override val gameHistory: GameHistory
) extends Game(id, position, gameState, gameHistory):

  override def gameType: GameType = Blackjack

  override protected def withGameState(newGameState: GameState): Game =
    copy(gameState = newGameState)

  override protected def withGameHistory(newGameHistory: GameHistory): Game =
    copy(gameHistory = newGameHistory)

  override def play[B <: Bet](bet: B): Result[BetResult, String] =
    bet match
      case b: BlackJackBet =>
        val strategy =
          use(BlackJackStrategy).bet(b.amount).accept(b.minimumValue).when(true)
        Success(strategy.use())
      case _ =>
        Failure(
          "Applied a bet different from the BlackJackBet to the blackjack!"
        )

  override val width: Double = 70.0
  override val height: Double = 40.0

object GameBuilder:

  private def generateId(prefix: String): String =
    s"$prefix-${Random.nextInt()}"

  def roulette(position: Vector2D): RouletteGame =
    RouletteGame(
      id = generateId("Roulette"),
      position = position,
      gameState = GameState(
        currentPlayers = 0,
        maxAllowedPlayers = 6,
        playersId = List.empty
      ),
      gameHistory = GameHistory(List.empty)
    )

  def slot(position: Vector2D): SlotMachineGame =
    SlotMachineGame(
      id = generateId("Slot"),
      position = position,
      gameState = GameState(
        currentPlayers = 0,
        maxAllowedPlayers = 1,
        playersId = List.empty
      ),
      gameHistory = GameHistory(List.empty)
    )

  def blackjack(position: Vector2D): BlackJackGame =
    BlackJackGame(
      id = generateId("BlackJack"),
      position = position,
      gameState = GameState(
        currentPlayers = 0,
        maxAllowedPlayers = 7,
        playersId = List.empty
      ),
      gameHistory = GameHistory(List.empty)
    )
