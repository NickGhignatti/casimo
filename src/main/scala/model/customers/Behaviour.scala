package model.customers

trait Behaviour[Context]:
  def apply[C <: Context](context: C): C
