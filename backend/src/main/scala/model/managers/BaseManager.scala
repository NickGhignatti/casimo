package model.managers

import model.GlobalConfig

trait BaseManager[A]:
  def update(slice: A)(using config: GlobalConfig): A
