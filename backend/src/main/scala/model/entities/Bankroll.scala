package model.entities

trait Bankroll[T <: Bankroll[T]]:
  val bankroll: Double

  def update(netValue: Double): T =
    updatedBankroll(bankroll + netValue)

  protected def updatedBankroll(newBankroll: Double): T
