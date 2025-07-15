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
    if (startTime > endTime) {
      if (time <= endTime || time >= startTime) highRate else lowRate
    } else if (time >= startTime && time <= endTime) highRate
    else lowRate

class SpawningStrategyBuilder:
  private var strategy: SpawningStrategy = ConstantStrategy(0)

  def constant(rate: Int): this.type =
    strategy = ConstantStrategy(rate)
    this

  def gaussian(peak: Double, mean: Double, stdDev: Double): this.type =
    strategy = GaussianStrategy(peak, mean, stdDev)
    this

  def step(lowRate: Int, highRate: Int, start: Double, end: Double): this.type =
    strategy = StepStrategy(lowRate, highRate, start, end)
    this

  def build(): SpawningStrategy = strategy

object SpawningStrategyBuilder:
  implicit class StrategyWrapper(strategy: SpawningStrategy):
    def +(offset: Int): SpawningStrategy =
      (time: Double) => strategy.customersAt(time) + offset

    def *(factor: Double): SpawningStrategy =
      (time: Double) => math.round(strategy.customersAt(time) * factor).toInt

  // Create a new builder instance
  def apply(): SpawningStrategyBuilder = new SpawningStrategyBuilder()
