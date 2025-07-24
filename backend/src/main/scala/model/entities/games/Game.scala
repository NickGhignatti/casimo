package model.entities.games

import scala.util.Random

import model.entities.CollidableEntity
import model.entities.games.dsl.use
import utils.Result
import utils.Result.Failure
import utils.Result.Success
import utils.Vector2D

/** Marker trait representing different types of casino games.
  *
  * Used for type-safe identification and categorization of game
  * implementations.
  */
trait GameType

/** Game type representing slot machine games */
object SlotMachine extends GameType

/** Game type representing roulette games */
object Roulette extends GameType

/** Game type representing blackjack games */
object Blackjack extends GameType

/** Abstract base trait for all casino games in the simulation.
  *
  * Represents a game entity that can be positioned in 2D space, maintains
  * player state, tracks game history, and supports collision detection. Games
  * can be locked/unlocked by players and process bets according to their
  * specific game rules.
  *
  * @param id
  *   unique identifier for the game instance
  * @param position
  *   2D coordinates of the game in the simulation space
  * @param gameState
  *   current state including players and capacity
  * @param gameHistory
  *   historical record of all gains and losses
  */
trait Game(
    val id: String,
    val position: Vector2D,
    val gameState: GameState,
    val gameHistory: GameHistory
) extends CollidableEntity:

  /** Returns the specific type of this game */
  def gameType: GameType

  /** Processes a bet according to the game's rules.
    *
    * @param bet
    *   the bet to be processed, must match the game's expected bet type
    * @return
    *   Success with BetResult if bet is valid, Failure with error message
    *   otherwise
    */
  def play[B <: Bet](bet: B): Result[BetResult, String]

  protected def withGameState(newGameState: GameState): Game
  protected def withGameHistory(newGameHistory: GameHistory): Game

  /** Attempts to lock the game for a specific player.
    *
    * @param id
    *   the player's unique identifier
    * @return
    *   Success with updated game if lock successful, Failure with unchanged
    *   game otherwise
    */
  def lock(id: String): Result[Game, Game] =
    gameState.addPlayer(id) match
      case Success(newGameState) => Success(withGameState(newGameState))
      case Failure(_)            => Failure(this)

  /** Attempts to unlock the game for a specific player.
    *
    * @param id
    *   the player's unique identifier
    * @return
    *   Success with updated game if unlock successful, Failure with unchanged
    *   game otherwise
    */
  def unlock(id: String): Result[Game, Game] =
    gameState.removePlayer(id) match
      case Success(newGameState) => Success(withGameState(newGameState))
      case Failure(_)            => Failure(this)

  /** Updates the game history with a new gain/loss entry for a customer.
    *
    * @param customerId
    *   the customer's unique identifier
    * @param gain
    *   the monetary gain (positive) or loss (negative)
    * @return
    *   new game instance with updated history
    */
  def updateHistory(customerId: String, gain: Double): Game =
    withGameHistory(gameHistory.update(customerId, gain))

  /** Returns true if the game has reached its maximum player capacity */
  def isFull: Boolean = gameState.isFull

  /** Returns the total bankroll (cumulative gains) for this game */
  def bankroll: Double = gameHistory.overallGains

  /** Returns the gains from the most recent round for current players */
  def getLastRoundResult: List[Gain] =
    gameHistory.gains.takeRight(gameState.currentPlayers)

/** Implementation of a roulette game.
  *
  * Roulette allows up to 6 players to bet on numbers 0-36. Uses
  * RouletteStrategy for game logic and bet processing.
  *
  * @param id
  *   unique identifier for this roulette game
  * @param position
  *   2D coordinates in the simulation space
  * @param gameState
  *   current player state and capacity
  * @param gameHistory
  *   historical record of gains and losses
  */
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

/** Implementation of a slot machine game.
  *
  * Slot machines are single-player games that accept simple monetary bets. Uses
  * SlotStrategy for game logic and payout calculations.
  *
  * @param id
  *   unique identifier for this slot machine
  * @param position
  *   2D coordinates in the simulation space
  * @param gameState
  *   current player state (max 1 player)
  * @param gameHistory
  *   historical record of gains and losses
  */
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

/** Implementation of a blackjack game.
  *
  * Blackjack supports up to 7 players and uses a minimum value strategy for
  * decision making. Uses BlackJackStrategy for game logic.
  *
  * @param id
  *   unique identifier for this blackjack game
  * @param position
  *   2D coordinates in the simulation space
  * @param gameState
  *   current player state and capacity
  * @param gameHistory
  *   historical record of gains and losses
  */
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

/** Factory object for creating game instances with default configurations.
  *
  * Provides convenient methods to create games with auto-generated IDs and
  * appropriate default settings for player capacity and initial state.
  */
object GameBuilder:

  private def generateId(prefix: String): String =
    s"$prefix-${Random.nextInt()}"

  /** Creates a new roulette game at the specified position.
    *
    * Initializes with capacity for 6 players and empty history.
    *
    * @param position
    *   the 2D coordinates for the game
    * @return
    *   a new RouletteGame instance
    */
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

  /** Creates a new slot machine game at the specified position.
    *
    * Initializes with capacity for 1 player and empty history.
    *
    * @param position
    *   the 2D coordinates for the game
    * @return
    *   a new SlotMachineGame instance
    */
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

  /** Creates a new blackjack game at the specified position.
    *
    * Initializes with capacity for 7 players and empty history.
    *
    * @param position
    *   the 2D coordinates for the game
    * @return
    *   a new BlackJackGame instance
    */
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
