package model.entities.customers

/** Defines the contract for an entity that experiences boredom and frustration.
  *
  * This trait allows entities to track and update their emotional states, which
  * can influence their decisions and behavior in the simulation. Boredom and
  * frustration values are typically represented as percentages (0-100).
  *
  * @tparam T
  *   The concrete type of the entity that extends this trait, enabling
  *   F-bounded polymorphism for immutable updates.
  */
trait BoredomFrustration[T <: BoredomFrustration[T]]:
  /** The current boredom level of the entity (0.0 - 100.0).
    */
  val boredom: Double

  /** The current frustration level of the entity (0.0 - 100.0).
    */
  val frustration: Double

  // Preconditions to ensure boredom and frustration are within valid percentage range
  require(
    boredom >= 0.0 && boredom <= 100.0,
    s"Boredom must be a percentile value, instead is $boredom %"
  )
  require(
    frustration >= 0.0 && frustration <= 100.0,
    s"Frustration must be a percentile value, instead is $frustration %"
  )

  /** Updates the entity's boredom level by adding a `boredomGain`. The new
    * boredom level is clamped between 0.0 and 100.0.
    *
    * @param boredomGain
    *   The amount to add to the current boredom level. Can be positive or
    *   negative.
    * @return
    *   A new instance of the entity with the updated boredom level.
    */
  def updateBoredom(boredomGain: Double): T =
    val newBoredom = boredom + boredomGain
    withBoredom((newBoredom max 0.0) min 100.0)

  /** Updates the entity's frustration level by adding a `frustrationGain`. The
    * new frustration level is clamped between 0.0 and 100.0.
    *
    * @param frustrationGain
    *   The amount to add to the current frustration level. Can be positive or
    *   negative.
    * @return
    *   A new instance of the entity with the updated frustration level.
    */
  def updateFrustration(frustrationGain: Double): T =
    val newFrustration = frustration + frustrationGain
    withFrustration((newFrustration max 0.0) min 100.0)

  /** Returns a new instance of the entity with an updated boredom level. This
    * method must be implemented by concrete classes to ensure immutability.
    *
    * @param newBoredom
    *   The new boredom level.
    * @return
    *   A new instance of the entity with the new boredom level.
    */
  def withBoredom(newBoredom: Double): T

  /** Returns a new instance of the entity with an updated frustration level.
    * This method must be implemented by concrete classes to ensure
    * immutability.
    *
    * @param newFrustration
    *   The new frustration level.
    * @return
    *   A new instance of the entity with the new frustration level.
    */
  def withFrustration(newFrustration: Double): T
