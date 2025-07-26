package model

import model.entities.games.Blackjack
import model.entities.games.GameType
import model.entities.games.Roulette
import model.entities.games.SlotMachine

/** Manages timing and scheduling for different simulation events and game
  * types.
  *
  * The Ticker provides a centralized timing system that coordinates when
  * different game types should process rounds and when customer spawning should
  * occur. It converts time intervals specified in seconds to tick-based
  * scheduling using the target framerate, ensuring consistent timing regardless
  * of actual frame rates.
  *
  * Each game type can have its own update frequency, allowing for realistic
  * timing where slot machines might update more frequently than roulette games,
  * which take longer to complete a round in real casinos.
  *
  * @param currentTick
  *   the current simulation tick counter
  * @param targetFramerate
  *   the desired frames per second for the simulation
  * @param slotInterval
  *   time in seconds between slot machine rounds
  * @param rouletteInterval
  *   time in seconds between roulette rounds
  * @param blackjackInterval
  *   time in seconds between blackjack rounds
  * @param spawnInterval
  *   time in seconds between customer spawning events
  */
case class Ticker(
    currentTick: Double,
    targetFramerate: Double = 60.0,
    slotInterval: Double = 0.2,
    rouletteInterval: Double = 1.0,
    blackjackInterval: Double = 0.7,
    spawnInterval: Double = 0.5
):

  /** Calculates the tick interval for slot machine updates.
    *
    * @return
    *   number of ticks between slot machine rounds
    */
  def slotTick: Double = slotInterval * targetFramerate

  /** Calculates the tick interval for roulette updates.
    *
    * @return
    *   number of ticks between roulette rounds
    */
  def rouletteTick: Double = rouletteInterval * targetFramerate

  /** Calculates the tick interval for blackjack updates.
    *
    * @return
    *   number of ticks between blackjack rounds
    */
  def blackjackTick: Double = blackjackInterval * targetFramerate

  /** Calculates the tick interval for customer spawning.
    *
    * @return
    *   number of ticks between spawning events
    */
  def spawnTick: Double = spawnInterval * targetFramerate

  /** Advances the ticker by one simulation tick.
    *
    * @return
    *   new Ticker instance with incremented tick counter
    */
  def update(): Ticker = copy(currentTick = currentTick + 1)

  /** Checks if a specific game type is ready to process its next round.
    *
    * Uses modulo arithmetic to determine if the current tick aligns with the
    * game's scheduled update interval. This ensures games update at their
    * intended frequencies regardless of the overall simulation speed.
    *
    * @param gameType
    *   the type of game to check
    * @return
    *   true if the game should process a round on this tick
    */
  def isGameReady(gameType: GameType): Boolean = gameType match
    case Blackjack   => currentTick % blackjackTick == 0
    case Roulette    => currentTick % rouletteTick == 0
    case SlotMachine => currentTick % slotTick == 0

  /** Checks if the simulation is ready to spawn new customers.
    *
    * @return
    *   true if customers should be spawned on this tick
    */
  def isReadyToSpawn: Boolean =
    currentTick % spawnTick == 0

  /** Creates a new Ticker with an updated target framerate.
    *
    * Changing the framerate affects all tick calculations, allowing for dynamic
    * adjustment of simulation speed while maintaining consistent timing
    * relationships between different game types.
    *
    * @param newFramerate
    *   the new target frames per second
    * @return
    *   new Ticker instance with updated framerate
    */
  def withFramerate(newFramerate: Double): Ticker =
    copy(targetFramerate = newFramerate)

  /** Creates a new Ticker with a specific current tick value.
    *
    * Useful for resetting the simulation time or jumping to a specific point in
    * the simulation timeline.
    *
    * @param currentTick
    *   the new current tick value
    * @return
    *   new Ticker instance with updated tick counter
    */
  def withCurrentTick(currentTick: Double): Ticker =
    copy(currentTick = currentTick)

  /** Creates a new Ticker with updated spawn timing.
    *
    * Allows dynamic adjustment of customer spawning frequency by specifying the
    * desired number of ticks between spawn events.
    *
    * @param noTicks
    *   the desired number of ticks between spawning events
    * @return
    *   new Ticker instance with updated spawn interval
    */
  def withSpawnTick(noTicks: Double): Ticker =
    copy(spawnInterval = noTicks / targetFramerate)

/** Factory object for creating Ticker instances with various configurations.
  */
object Ticker:
  /** Creates a new Ticker with default intervals and specified framerate.
    *
    * Uses default timing intervals suitable for typical casino simulation:
    *   - Slot machines: 0.2 seconds (fast-paced)
    *   - Roulette: 1.0 seconds (moderate pace)
    *   - Blackjack: 0.7 seconds (card dealing speed)
    *   - Spawning: 0.5 seconds (regular customer arrival)
    *
    * @param framerate
    *   the target frames per second for the simulation
    * @return
    *   new Ticker instance with default intervals
    */
  def apply(framerate: Double): Ticker =
    Ticker(currentTick = 0, targetFramerate = framerate)

  /** Creates a new Ticker with custom intervals for all game types and
    * spawning.
    *
    * Allows full customization of timing for different simulation aspects,
    * useful for creating different casino atmospheres or testing scenarios.
    *
    * @param framerate
    *   the target frames per second
    * @param slotIntervalSeconds
    *   time in seconds between slot machine rounds
    * @param rouletteIntervalSeconds
    *   time in seconds between roulette rounds
    * @param blackjackIntervalSeconds
    *   time in seconds between blackjack rounds
    * @param spawnIntervalSeconds
    *   time in seconds between customer spawning
    * @return
    *   new Ticker instance with custom intervals
    */
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
