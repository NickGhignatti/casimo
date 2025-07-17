package model.entities.games

import org.scalatest.funsuite.AnyFunSuite
import utils.Result.Failure
import utils.Result.Success
import utils.Vector2D

class TestGame extends AnyFunSuite:
  val mockId = "test"

  test("lock should succeed when under capacity"):
    val game = GameBuilder.slot(Vector2D.zero)
    game.lock(mockId) match
      case Success(newGame) => assert(newGame.gameState.currentPlayers === 1)
      case _                => fail("Expected Success when locking game")

  test("lock should fail when at capacity"):
    val game = GameBuilder.slot(Vector2D.zero)
    val first = game.lock(mockId)
    assert(first.isSuccess, "First lock should succeed")

    val lockedGame = first.getOrElse(game)
    lockedGame.lock(mockId) match
      case Failure(newGame) => assert(newGame === lockedGame)
      case _                => fail("Expected Failure when locking full game")

  test("unlock should succeed when players present"):
    val game = GameBuilder.slot(Vector2D.zero)
    val first = game.lock(mockId)
    assert(first.isSuccess, "First lock should succeed")
    first.getOrElse(game).unlock(mockId) match
      case Success(newGame) => assert(newGame.gameState.currentPlayers === 0)
      case _                => fail("Expected Success when unlocking game")

  test("unlock should fail when no players"):
    val game = GameBuilder.slot(Vector2D.zero)
    game.unlock(mockId) match
      case Failure(newGame) => assert(newGame === game)
      case _ => fail("Expected Failure when unlocking empty game")

  test("GameType should be roulette when created a roulette"):
    val game = GameBuilder.roulette(Vector2D.zero)
    assert(game.gameType == Roulette)

  test("GameType should be slot when created a slot"):
    val game = GameBuilder.slot(Vector2D.zero)
    assert(game.gameType == SlotMachine)

  test("GameType should be blackjack when created a blackjack"):
    val game = GameBuilder.blackjack(Vector2D.zero)
    assert(game.gameType == Blackjack)

  test("Roulette should not fail if applied RouletteBet"):
    val game = GameBuilder.roulette(Vector2D.zero)
    assert(game.play(RouletteBet(10.0, List(10))).isSuccess)

  test("Roulette should fail if not applied RouletteBet"):
    val game = GameBuilder.roulette(Vector2D.zero)
    assert(game.play(SlotBet(10.0)).isFailure)

  test("SlotMachine should not fail if applied SlotBet"):
    val game = GameBuilder.slot(Vector2D.zero)
    assert(game.play(SlotBet(10.0)).isSuccess)

  test("SlotMachine should fail if not applied SlotBet"):
    val game = GameBuilder.slot(Vector2D.zero)
    assert(game.play(RouletteBet(10.0, List(10))).isFailure)

  test("BlackJack should not fail if applied BlackJackBet"):
    val game = GameBuilder.blackjack(Vector2D.zero)
    assert(game.play(BlackJackBet(10.0, 18)).isSuccess)

  test("BlackJack should fail if not applied RouletteBet"):
    val game = GameBuilder.blackjack(Vector2D.zero)
    assert(game.play(SlotBet(10.0)).isFailure)
