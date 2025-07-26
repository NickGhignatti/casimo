package utils

/** A type representing either a successful value or an error.
  *
  * `Result[T, E]` is similar to `Either[E, T]` but with more intuitive naming
  * where `T` represents the success type and `E` represents the error type.
  * This is commonly used for error handling without exceptions.
  *
  * @tparam T
  *   the type of the success value
  * @tparam E
  *   the type of the error value
  * @example
  *   {{{ val success: Result[Int, String] = Result.Success(42) val failure:
  *   Result[Int, String] = Result.Failure("Something went wrong")
  *
  * val doubled = success.map(_ * 2) // Result.Success(84) val errorResult =
  * failure.map(_ * 2) // Result.Failure("Something went wrong") }}}
  */
enum Result[+T, +E]:
  /** Represents a successful result containing a value of type `T`.
    *
    * @param value
    *   the successful value
    */
  case Success(value: T)

  /** Represents a failed result containing an error of type `E`.
    *
    * @param error
    *   the error value
    */
  case Failure(error: E)

  /** Transforms the success value using the provided function, leaving failures
    * unchanged.
    *
    * @param f
    *   the function to apply to the success value
    * @tparam U
    *   the type of the transformed value
    * @return
    *   a new Result with the transformed value if this is a Success, or the
    *   same Failure if this is a Failure
    */
  def map[U](f: T => U): Result[U, E] = this match
    case Success(value) => Success(f(value))
    case Failure(error) => Failure(error)

  /** Applies a function that returns a Result to the success value, flattening
    * the result. This is useful for chaining operations that might fail.
    *
    * @param f
    *   the function to apply to the success value
    * @tparam U
    *   the success type of the returned Result
    * @tparam F
    *   the error type of the returned Result
    * @return
    *   the result of applying f if this is a Success, or the same Failure if
    *   this is a Failure
    */
  def flatMap[U, F](f: T => Result[U, F]): Result[U, E | F] = this match
    case Success(value) => f(value)
    case Failure(error) => Failure(error)

  /** Returns the success value if this is a Success, otherwise returns the
    * default value.
    *
    * @param default
    *   the value to return if this is a Failure
    * @tparam U
    *   the type of the default value (must be a supertype of T)
    * @return
    *   the success value or the default value
    */
  def getOrElse[U >: T](default: U): U = this match
    case Success(value) => value
    case Failure(_)     => default

  /** Returns true if this Result is a Success, false otherwise.
    *
    * @return
    *   true if Success, false if Failure
    */
  def isSuccess: Boolean = this match
    case Success(_) => true
    case Failure(_) => false

  /** Returns true if this Result is a Failure, false otherwise.
    *
    * @return
    *   true if Failure, false if Success
    */
  def isFailure: Boolean = !isSuccess

/** Companion object for Result containing utility methods and extensions.
  */
object Result:
  extension [A](result: Result[A, A])
    /** Converts a Result where both success and error types are the same to an
      * Option. Success values are converted to Some, and Failure values are
      * converted to None.
      *
      * @return
      *   Some(value) if Success, None if Failure
      */
    def option(): Option[A] =
      result match
        case Result.Success(value) => Some(value)
        case _                     => None
