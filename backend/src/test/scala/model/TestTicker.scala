package model

import model.entities.games.{Blackjack, Roulette, SlotMachine}
import org.scalatest.funsuite.AnyFunSuite

class TestTicker extends AnyFunSuite:
  val baseTicker: Ticker = Ticker(60)

  test("default builder should work correctly"):
    assert(baseTicker.currentTick == 0)
    assert(baseTicker.targetFramerate == 60)
    assert(baseTicker.slotTick == 12)
    assert(baseTicker.rouletteTick == 60)
    assert(baseTicker.blackjackTick == 42)
    assert(baseTicker.spawnTick == 30)

  test("builder should work correctly"):
    val interval = 1.0 / 30.0
    val ticker = Ticker(30, interval, interval, interval, interval)

    assert(ticker.currentTick == 0)
    assert(ticker.targetFramerate == 30)
    assert(ticker.slotTick == 1)
    assert(ticker.rouletteTick == 1)
    assert(ticker.blackjackTick == 1)
    assert(ticker.spawnTick == 1)

  test("SlotTick should work"):
    val ticked =
      (0 until baseTicker.slotTick.toInt).foldLeft(baseTicker)((t, _) =>
        t.update()
      )

    assert(ticked.isGameReady(SlotMachine))

  test("RouletteTick should work"):
    val ticked =
      (0 until baseTicker.rouletteTick.toInt).foldLeft(baseTicker)((t, _) =>
        t.update()
      )

    assert(ticked.isGameReady(Roulette))

  test("BlackJackTick should work"):
    val ticked =
      (0 until baseTicker.blackjackTick.toInt).foldLeft(baseTicker)((t, _) =>
        t.update()
      )

    assert(ticked.isGameReady(Blackjack))

  test("isReadyToSpawn should work"):
    val ticked =
      (0 until baseTicker.spawnTick.toInt).foldLeft(baseTicker)((t, _) =>
        t.update()
      )

    assert(ticked.isReadyToSpawn)

  test("Base Ticker should have correct number of ticks for each game"):
    assert(baseTicker.slotTick == 12)
    assert(baseTicker.rouletteTick == 60)
    assert(baseTicker.blackjackTick == 42)
    assert(baseTicker.spawnTick == 30)

  test(
    "Updated Ticker with a new Framerate should update also the number of ticks"
  ):
    val updatedTicker = baseTicker.withFramerate(30)

    assert(updatedTicker.slotTick == 6)
    assert(updatedTicker.rouletteTick == 30)
    assert(updatedTicker.blackjackTick == 21)
    assert(updatedTicker.spawnTick == 15)

  test("withCurrentTick should update the currentTick of the Ticker"):
    assert(baseTicker.currentTick == 0)
    assert(baseTicker.withCurrentTick(10).currentTick == 10)

  test("withSpawnTick should update the spawnTick"):
    val newTicker = baseTicker.withSpawnTick(1)

    assert(newTicker.spawnTick == 1)
    assert(newTicker.spawnInterval == (1.0 / 60.0))
