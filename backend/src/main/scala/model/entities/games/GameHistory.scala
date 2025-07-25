package model.entities.games

/** Represents a single monetary gain or loss record for a specific customer.
  *
  * Encapsulates the result of a game round, tracking which customer
  * participated and the monetary outcome. Positive values represent gains
  * (winnings) while negative values represent losses.
  *
  * @param from
  *   the unique identifier of the customer who played
  * @param of
  *   the monetary amount gained (positive) or lost (negative)
  */
class Gain(from: String, of: Double):

  /** Returns the monetary gain or loss amount.
    *
    * @return
    *   positive value for winnings, negative value for losses
    */
  def getMoneyGain: Double = this.of

  /** Returns the identifier of the customer who played.
    *
    * @return
    *   the unique customer identifier
    */
  def getCustomerWhichPlayed: String = this.from

/** Maintains the complete history of all gains and losses for a game.
  *
  * Tracks all monetary transactions that have occurred during the game's
  * lifetime, providing methods to calculate overall performance and add new
  * entries. The history is immutable - updates create new instances.
  *
  * @param gains
  *   chronological list of all gain/loss records
  */
case class GameHistory(gains: List[Gain]):

  /** Calculates the total cumulative gains across all recorded transactions.
    *
    * Sums all individual gains and losses to determine the overall financial
    * performance of the game.
    *
    * @return
    *   the sum of all gains and losses, representing net game performance
    */
  def overallGains: Double = gains.map(_.getMoneyGain).sum

  /** Creates a new GameHistory with an additional gain/loss record.
    *
    * Appends a new gain entry to the existing history, maintaining
    * chronological order
    *
    * @param customerId
    *   the unique identifier of the customer
    * @param gain
    *   the monetary amount (positive for winnings, negative for losses)
    * @return
    *   new GameHistory instance with the additional record
    */
  def update(customerId: String, gain: Double): GameHistory =
    this.copy(gains = gains :+ Gain(customerId, gain))
