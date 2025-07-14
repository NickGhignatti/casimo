package model.managers

import model.GlobalConfig

trait BaseManager[A]:
  def update(slice: A)(using config: GlobalConfig): A

extension [A](first: BaseManager[A])
  def |(second: BaseManager[A]): BaseManager[A] =
    new BaseManager[A]:
      override def update(slice: A)(using config: GlobalConfig): A =
        second.update(first.update(slice))

extension [A](slice: A)
  def |(manager: BaseManager[A])(using GlobalConfig): A = manager.update(slice)
