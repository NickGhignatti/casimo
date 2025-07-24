package model

import model.entities.games.Blackjack
import model.entities.games.GameType
import model.entities.games.Roulette
import model.entities.games.SlotMachine

case class Ticker(
    currentTick: Double,
    slotTick: Double = 12,
    rouletteTick: Double = 60,
    blackjackTick: Double = 42,
    spawnTick: Double = 30
):
  def update(): Ticker = copy(currentTick = currentTick + 1)

  def isGameReady(gameType: GameType): Boolean = gameType match
    case Blackjack   => currentTick % blackjackTick == 0
    case Roulette    => currentTick % rouletteTick == 0
    case SlotMachine => currentTick % slotTick == 0

  def isReadyToSpawn: Boolean =
    currentTick % spawnTick == 0
