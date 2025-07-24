package model.entities.spawner

trait SpawningStrategy:
  def customersAt(time: Double): Int

case class ConstantStrategy(rate: Int) extends SpawningStrategy:
  override def customersAt(time: Double): Int = rate

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

class SpawningStrategyBuilder private (private val strategy: SpawningStrategy):
  def this() = this(ConstantStrategy(0))

  def constant(rate: Int): SpawningStrategyBuilder =
    new SpawningStrategyBuilder(ConstantStrategy(rate))

  def gaussian(
      peak: Double,
      mean: Double,
      stdDev: Double
  ): SpawningStrategyBuilder =
    new SpawningStrategyBuilder(GaussianStrategy(peak, mean, stdDev))

  def step(
      lowRate: Int,
      highRate: Int,
      start: Double,
      end: Double
  ): SpawningStrategyBuilder =
    new SpawningStrategyBuilder(StepStrategy(lowRate, highRate, start, end))

  def custom(f: Double => Int): SpawningStrategyBuilder =
    new SpawningStrategyBuilder((time: Double) => f(time))

  // DSL operations
  def offset(amount: Int): SpawningStrategyBuilder =
    require(amount >= 0)
    val newStrategy = new SpawningStrategy:
      override def customersAt(time: Double): Int =
        strategy.customersAt(time) + amount
    new SpawningStrategyBuilder(newStrategy)

  def scale(factor: Double): SpawningStrategyBuilder =
    require(factor >= 0, "scale factor should be >= 0")
    val newStrategy = new SpawningStrategy:
      override def customersAt(time: Double): Int =
        math.round(strategy.customersAt(time) * factor).toInt
    new SpawningStrategyBuilder(newStrategy)

  def clamp(min: Int, max: Int): SpawningStrategyBuilder =
    require(min >= 0, "minimum value should be >= 0")
    require(min <= max, "maximum value should be greater than minimum")
    val newStrategy = new SpawningStrategy:
      override def customersAt(time: Double): Int =
        val value = strategy.customersAt(time)
        value.max(min).min(max)
    new SpawningStrategyBuilder(newStrategy)

  def build(): SpawningStrategy = strategy

object SpawningStrategyBuilder:
  implicit class StrategyWrapper(strategy: SpawningStrategy):
    def +(offset: Int): SpawningStrategy =
      (time: Double) => strategy.customersAt(time) + offset

    def *(factor: Double): SpawningStrategy =
      (time: Double) => math.round(strategy.customersAt(time) * factor).toInt

  // Create a new builder instance
  def apply(): SpawningStrategyBuilder = new SpawningStrategyBuilder()
