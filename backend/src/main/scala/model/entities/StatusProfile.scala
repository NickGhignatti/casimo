package model.entities

trait StatusProfile:
  val riskProfile: RiskProfile

enum RiskProfile:
  case VIP, Regular, Casual
