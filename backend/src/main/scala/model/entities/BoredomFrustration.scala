package model.entities

trait BoredomFrustration[T <: BoredomFrustration[T]]:
  val boredom: Double
  val frustration: Double

  def updateBoredom(boredomGain: Double): T =
    val newBoredom = boredom + boredomGain
    updatedBoredom((newBoredom max 0.0) min 100.0)

  def updateFrustration(frustrationGain: Double): T =
    val newFrustration = frustration + frustrationGain
    updatedFrustration((newFrustration max 0.0) min 100.0)

  protected def updatedBoredom(newBoredom: Double): T

  protected def updatedFrustration(newFrustration: Double): T
