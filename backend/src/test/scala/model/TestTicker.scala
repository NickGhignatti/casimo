package model

import model.entities.games.{Blackjack, Roulette, SlotMachine}
import org.scalatest.funsuite.AnyFunSuite

class TestTicker extends AnyFunSuite:
  val baseTicker: Ticker = Ticker(60)

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
