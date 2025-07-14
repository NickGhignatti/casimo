package model.entities.customers

trait StatusProfile:
  val riskProfile: RiskProfile

enum RiskProfile:
  case VIP, Regular, Casual
