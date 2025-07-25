package model

import model.entities.games.Blackjack
import model.entities.games.GameType
import model.entities.games.Roulette
import model.entities.games.SlotMachine

case class Ticker(
    currentTick: Double,
    targetFramerate: Double = 60.0,
    slotInterval: Double = 0.2,
    rouletteInterval: Double = 1.0,
    blackjackInterval: Double = 0.7,
    spawnInterval: Double = 0.5
):
  def slotTick: Double = slotInterval * targetFramerate
  def rouletteTick: Double = rouletteInterval * targetFramerate
  def blackjackTick: Double = blackjackInterval * targetFramerate
  def spawnTick: Double = spawnInterval * targetFramerate

  def update(): Ticker = copy(currentTick = currentTick + 1)

  def isGameReady(gameType: GameType): Boolean = gameType match
    case Blackjack   => currentTick % blackjackTick == 0
    case Roulette    => currentTick % rouletteTick == 0
    case SlotMachine => currentTick % slotTick == 0

  def isReadyToSpawn: Boolean =
    currentTick % spawnTick == 0

  def withFramerate(newFramerate: Double): Ticker =
    copy(targetFramerate = newFramerate)

  def withCurrentTick(currentTick: Double): Ticker =
    copy(currentTick = currentTick)

  def withSpawnTick(noTicks: Double): Ticker =
    copy(spawnInterval = noTicks / targetFramerate)

object Ticker:
  def apply(framerate: Double): Ticker =
    Ticker(currentTick = 0, targetFramerate = framerate)

  def apply(
      framerate: Double,
      slotIntervalSeconds: Double,
      rouletteIntervalSeconds: Double,
      blackjackIntervalSeconds: Double,
      spawnIntervalSeconds: Double
  ): Ticker =
    Ticker(
      currentTick = 0,
      targetFramerate = framerate,
      slotInterval = slotIntervalSeconds,
      rouletteInterval = rouletteIntervalSeconds,
      blackjackInterval = blackjackIntervalSeconds,
      spawnInterval = spawnIntervalSeconds
    )
