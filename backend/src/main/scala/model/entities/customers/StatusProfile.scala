package model.entities.customers

/** Defines the contract for an entity that possesses a risk profile.
  *
  * This trait is used to categorize customers based on their behavior patterns
  * within the casino, influencing decisions made by managers like the
  * `DecisionManager`.
  */
trait StatusProfile:
  /** The risk profile associated with this entity.
    */
  val riskProfile: RiskProfile

/** Enumeration representing different risk profiles for customers.
  *
  * These profiles categorize customers' willingness to take risks and influence
  * their decision-making, betting strategies, and reactions to game outcomes.
  */
enum RiskProfile:
  /** VIP (Very Important Person) customer profile. */
  case VIP

  /** Regular customer profile. */
  case Regular

  /** Casual customer profile. */
  case Casual

  /** Impulsive customer profile. */
  case Impulsive
