package model.entities

trait BoredomFrustration[T <: BoredomFrustration[T]]:
  val boredom: Double
  val frustration: Double

  def updateBoredom(boredomGain: Double): T =
    updatedBoredom(boredom + boredomGain)

  def updateFrustration(frustrationGain: Double): T =
    updatedFrustration(frustration + frustrationGain)

  protected def updatedBoredom(newBoredom: Double): T

  protected def updatedFrustration(newFrustration: Double): T
