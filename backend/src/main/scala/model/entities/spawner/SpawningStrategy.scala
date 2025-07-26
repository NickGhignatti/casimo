package model.entities.spawner

/** Base trait for all customer spawning strategies.
  *
  * Defines the interface for determining how many customers should be spawned
  * at any given time point in the simulation. Implementations can provide
  * various distribution patterns such as constant rates, gaussian curves, or
  * step functions.
  */
trait SpawningStrategy:
  /** Calculates the number of customers to spawn at the given time.
    *
    * @param time
    *   the current simulation time point
    * @return
    *   the number of customers to spawn (must be non-negative)
    */
  def customersAt(time: Double): Int

/** A spawning strategy that maintains a constant rate of customer creation.
  *
  * Simple strategy that spawns the same number of customers at every time
  * point, useful for maintaining steady simulation load or baseline customer
  * flow.
  *
  * @param rate
  *   the constant number of customers to spawn per time unit
  */
case class ConstantStrategy(rate: Int) extends SpawningStrategy:
  override def customersAt(time: Double): Int = rate

/** A spawning strategy that follows a Gaussian (normal) distribution curve.
  *
  * Creates a bell-curve pattern of customer spawning, with peak activity at the
  * mean time and decreasing activity further from the center. Useful for
  * modeling natural patterns like rush hours or peak casino times.
  *
  * @param peak
  *   the maximum number of customers to spawn at the distribution peak
  * @param mean
  *   the time point where spawning activity is highest
  * @param stdDev
  *   the standard deviation controlling the width of the distribution
  * @param base
  *   the minimum baseline number of customers to always spawn
  */
case class GaussianStrategy(
    peak: Double,
    mean: Double,
    stdDev: Double,
    base: Int = 0
) extends SpawningStrategy:
  override def customersAt(time: Double): Int =
    if (stdDev <= 0) {
      if (math.abs(time - mean) < 1e-9) (base + peak).toInt else base
    } else {
      val exponent = -0.5 * math.pow((time - mean) / stdDev, 2)
      val value = base + peak * math.exp(exponent)
      math.round(value).toInt.max(0)
    }

/** A spawning strategy that switches between two rates at specified time
  * boundaries.
  *
  * Provides a step function for customer spawning, switching between low and
  * high rates based on time intervals. Supports both normal intervals (start <
  * end) and wrap-around intervals (start > end) for modeling scenarios like
  * overnight periods or day/night cycles.
  *
  * @param lowRate
  *   the number of customers to spawn during low-activity periods
  * @param highRate
  *   the number of customers to spawn during high-activity periods
  * @param startTime
  *   the beginning of the high-activity period
  * @param endTime
  *   the end of the high-activity period
  */
case class StepStrategy(
    lowRate: Int,
    highRate: Int,
    startTime: Double,
    endTime: Double
) extends SpawningStrategy:
  override def customersAt(time: Double): Int =
    if (startTime > endTime) then
      if (time <= endTime || time >= startTime) then highRate else lowRate
    else if (time >= startTime && time <= endTime) then highRate
    else lowRate

/** Builder class for constructing and composing spawning strategies using a
  * fluent API.
  *
  * Provides a convenient way to create complex spawning strategies by combining
  * basic strategies with transformations like scaling, offsetting, and
  * clamping. Supports method chaining for readable strategy composition.
  *
  * @param strategy
  *   the current strategy being built
  */
class SpawningStrategyBuilder private (private val strategy: SpawningStrategy):
  /** Creates a new builder with a default constant strategy of rate 0.
    */
  def this() = this(ConstantStrategy(0))

  /** Sets the strategy to a constant spawning rate.
    *
    * @param rate
    *   the constant number of customers to spawn per time unit
    * @return
    *   new builder with the constant strategy
    */
  def constant(rate: Int): SpawningStrategyBuilder =
    new SpawningStrategyBuilder(ConstantStrategy(rate))

  /** Sets the strategy to a Gaussian distribution pattern.
    *
    * @param peak
    *   the maximum number of customers at the distribution peak
    * @param mean
    *   the time point of peak activity
    * @param stdDev
    *   the standard deviation of the distribution
    * @return
    *   new builder with the Gaussian strategy
    */
  def gaussian(
      peak: Double,
      mean: Double,
      stdDev: Double
  ): SpawningStrategyBuilder =
    new SpawningStrategyBuilder(GaussianStrategy(peak, mean, stdDev))

  /** Sets the strategy to a step function with two distinct rates.
    *
    * @param lowRate
    *   customers per time unit during low-activity periods
    * @param highRate
    *   customers per time unit during high-activity periods
    * @param start
    *   beginning of the high-activity period
    * @param end
    *   end of the high-activity period
    * @return
    *   new builder with the step strategy
    */
  def step(
      lowRate: Int,
      highRate: Int,
      start: Double,
      end: Double
  ): SpawningStrategyBuilder =
    new SpawningStrategyBuilder(StepStrategy(lowRate, highRate, start, end))

  /** Sets the strategy to a custom function.
    *
    * @param f
    *   function that maps time to customer count
    * @return
    *   new builder with the custom strategy
    */
  def custom(f: Double => Int): SpawningStrategyBuilder =
    new SpawningStrategyBuilder((time: Double) => f(time))

  // DSL operations

  /** Adds a constant offset to the current strategy's output.
    *
    * @param amount
    *   the number of additional customers to spawn (must be non-negative)
    * @return
    *   new builder with offset applied
    * @throws IllegalArgumentException
    *   if amount is negative
    */
  def offset(amount: Int): SpawningStrategyBuilder =
    require(amount >= 0)
    val newStrategy = new SpawningStrategy:
      override def customersAt(time: Double): Int =
        strategy.customersAt(time) + amount
    new SpawningStrategyBuilder(newStrategy)

  /** Scales the current strategy's output by a multiplication factor.
    *
    * @param factor
    *   the scaling factor to apply (must be non-negative)
    * @return
    *   new builder with scaling applied
    * @throws IllegalArgumentException
    *   if factor is negative
    */
  def scale(factor: Double): SpawningStrategyBuilder =
    require(factor >= 0, "scale factor should be >= 0")
    val newStrategy = new SpawningStrategy:
      override def customersAt(time: Double): Int =
        math.round(strategy.customersAt(time) * factor).toInt
    new SpawningStrategyBuilder(newStrategy)

  /** Clamps the current strategy's output within specified bounds.
    *
    * Ensures the spawning count never goes below the minimum or above the
    * maximum, useful for preventing extreme values in complex strategy
    * compositions.
    *
    * @param min
    *   the minimum number of customers to spawn (must be non-negative)
    * @param max
    *   the maximum number of customers to spawn (must be >= min)
    * @return
    *   new builder with clamping applied
    * @throws IllegalArgumentException
    *   if constraints are violated
    */
  def clamp(min: Int, max: Int): SpawningStrategyBuilder =
    require(min >= 0, "minimum value should be >= 0")
    require(min <= max, "maximum value should be greater than minimum")
    val newStrategy = new SpawningStrategy:
      override def customersAt(time: Double): Int =
        val value = strategy.customersAt(time)
        value.max(min).min(max)
    new SpawningStrategyBuilder(newStrategy)

  /** Finalizes the builder and returns the constructed strategy.
    *
    * @return
    *   the final SpawningStrategy instance
    */
  def build(): SpawningStrategy = strategy

/** Companion object providing factory methods and implicit conversions for
  * spawning strategies.
  */
object SpawningStrategyBuilder:

  /** Provides operator overloads for convenient strategy composition.
    *
    * Enables mathematical operations on SpawningStrategy instances using
    * natural syntax like `strategy + 5` or `strategy * 2.0`.
    */
  implicit class StrategyWrapper(strategy: SpawningStrategy):

    /** Adds a constant offset to the strategy's output.
      *
      * @param offset
      *   the number to add to each spawning count
      * @return
      *   new strategy with offset applied
      */
    def +(offset: Int): SpawningStrategy =
      (time: Double) => strategy.customersAt(time) + offset

    /** Multiplies the strategy's output by a scaling factor.
      *
      * @param factor
      *   the multiplication factor to apply
      * @return
      *   new strategy with scaling applied
      */
    def *(factor: Double): SpawningStrategy =
      (time: Double) => math.round(strategy.customersAt(time) * factor).toInt

  /** Creates a new SpawningStrategyBuilder instance.
    *
    * @return
    *   a new builder with default constant strategy of rate 0
    */
  def apply(): SpawningStrategyBuilder = new SpawningStrategyBuilder()
