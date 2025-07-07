package model.entities.customers

trait BoredomFrustration[T <: BoredomFrustration[T]]:
  val boredom: Double
  val frustration: Double
  require(
    boredom >= 0.0 && boredom <= 100.0,
    s"Boredom must be a percentile value, instead is $boredom %"
  )
  require(
    frustration >= 0.0 && frustration <= 100.0,
    s"Frustration must be a percentile value, instead is $frustration %"
  )

  def updateBoredom(boredomGain: Double): T =
    val newBoredom = boredom + boredomGain
    updatedBoredom((newBoredom max 0.0) min 100.0)

  def updateFrustration(frustrationGain: Double): T =
    val newFrustration = frustration + frustrationGain
    updatedFrustration((newFrustration max 0.0) min 100.0)

  protected def updatedBoredom(newBoredom: Double): T

  protected def updatedFrustration(newFrustration: Double): T
