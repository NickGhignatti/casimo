package model.managers

trait BaseManager[A]:
  def update(slice: A): A

extension [A](first: BaseManager[A])
  def |(second: BaseManager[A]): BaseManager[A] =
    new BaseManager[A]:
      override def update(slice: A): A =
        second.update(first.update(slice))

extension [A](slice: A)
  def |(manager: BaseManager[A]): A = manager.update(slice)
