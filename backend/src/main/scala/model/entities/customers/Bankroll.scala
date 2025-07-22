package model.entities.customers

trait Bankroll[T <: Bankroll[T]]:
  val bankroll: Double
  val startingBankroll: Double
  require(
    bankroll >= 0,
    s"Bankroll amount must be positive, instead is $bankroll"
  )

  def updateBankroll(netValue: Double): T =
    val newBankroll = bankroll + netValue
    require(
      newBankroll >= 0,
      s"Bankroll amount must be positive, instead is $newBankroll"
    )
    withBankroll(newBankroll)

  def withBankroll(newBankroll: Double): T
