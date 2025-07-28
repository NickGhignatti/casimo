package model.managers

/** Base trait for all managers in the simulation.
  *
  * Defines a fundamental contract for components that update a "slice" of the
  * simulation state. Managers are designed to be functional, taking an input
  * state and returning a new, updated state, promoting immutability.
  *
  * @tparam A
  *   The type of the state slice that this manager operates on.
  */
trait BaseManager[A]:
  /** Updates a given slice of the simulation state.
    *
    * This method encapsulates the core logic of the manager, transforming an
    * input state into an updated state.
    *
    * @param slice
    *   The input state slice to be updated.
    * @return
    *   The updated state slice.
    */
  def update(slice: A): A

/** Provides extension methods for `BaseManager` to enable fluent chaining. This
  * allows managers to be composed using the `|` operator, creating processing
  * pipelines for state updates.
  */
extension [A](first: BaseManager[A])
  /** Chains two BaseManager instances together. The output of the `first`
    * manager becomes the input of the `second` manager.
    *
    * @param second
    *   The second BaseManager in the chain.
    * @return
    *   A new BaseManager that represents the sequential application of `first`
    *   then `second`.
    */
  def |(second: BaseManager[A]): BaseManager[A] =
    new BaseManager[A]:
      override def update(slice: A): A =
        second.update(first.update(slice))

/** Provides an extension method for a state slice to apply a manager to itself.
  * This allows for a fluent syntax like `mySlice | myManager`, applying the
  * manager's update logic directly to the slice.
  */
extension [A](slice: A)
  /** Applies a BaseManager's update logic to the current state slice.
    * @param manager
    *   The BaseManager to apply.
    * @return
    *   The updated state slice after applying the manager.
    */
  def |(manager: BaseManager[A]): A = manager.update(slice)
