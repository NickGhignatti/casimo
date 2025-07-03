package model.entities

trait CustomerState:
  val customerState: CustState

enum CustState:
  case Playing, Idle, Exited
