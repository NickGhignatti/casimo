package utils

/** Represents a generic decision tree structure.
  *
  * This sealed trait provides the common interface for all nodes within the
  * decision tree, ensuring that any concrete node can evaluate a given context
  * to produce a result. The use of a sealed trait ensures that all possible
  * implementations are known at compile time, enabling exhaustive pattern
  * matching and enhancing type safety.
  *
  * @tparam Ctx
  *   The type of the context (input) on which the decision tree operates.
  * @tparam Res
  *   The type of the result (output) produced by the decision tree.
  */
sealed trait DecisionTree[Ctx, Res]:
  /** Evaluates the decision tree (or subtree) with the given context.
    *
    * This method traverses the tree structure, applying predicates or key
    * lookups until a leaf node is reached, which then produces the final
    * result.
    *
    * @param ctx
    *   The context object used for evaluation.
    * @return
    *   The result produced by the decision tree based on the context.
    */
  def eval(ctx: Ctx): Res

/** Represents a leaf node in the decision tree.
  *
  * A Leaf node is a terminal point in the tree, directly providing a result
  * based on an associated action function without further branching.
  *
  * @param action
  *   A function that takes the context and produces the final result for this
  *   leaf.
  * @tparam Ctx
  *   The context type.
  * @tparam Res
  *   The result type.
  */
case class Leaf[Ctx, Res](action: Ctx => Res) extends DecisionTree[Ctx, Res]:
  /** Evaluates the Leaf node by applying its action function to the context.
    * @param ctx
    *   The context object.
    * @return
    *   The result produced by the action.
    */
  override def eval(ctx: Ctx): Res = action(ctx)

/** Represents a binary decision node in the decision tree.
  *
  * A DecisionNode evaluates a predicate against the context and branches to
  * either a trueBranch or a falseBranch accordingly.
  *
  * @param predicate
  *   A function that takes the context and returns a boolean, determining the
  *   branch to follow.
  * @param trueBranch
  *   The DecisionTree to evaluate if the predicate returns true.
  * @param falseBranch
  *   The DecisionTree to evaluate if the predicate returns false.
  * @tparam Ctx
  *   The context type.
  * @tparam Res
  *   The result type.
  */
case class DecisionNode[Ctx, Res](
    predicate: Ctx => Boolean,
    trueBranch: DecisionTree[Ctx, Res],
    falseBranch: DecisionTree[Ctx, Res]
) extends DecisionTree[Ctx, Res]:
  /** Evaluates the DecisionNode by checking its predicate and recursing into
    * the appropriate branch.
    * @param ctx
    *   The context object.
    * @return
    *   The result from the chosen branch.
    */
  override def eval(ctx: Ctx): Res =
    if predicate(ctx) then trueBranch.eval(ctx)
    else falseBranch.eval(ctx)

/** Represents a multi-way decision node in the decision tree.
  *
  * A MultiNode dispatches to one of several branches based on a key extracted
  * from the context. It includes a default branch for keys that do not have a
  * specific corresponding branch.
  *
  * @param keyOf
  *   A function that extracts a key from the context to select a branch.
  * @param branches
  *   A map associating keys with their respective decision tree branches.
  * @param default
  *   The default DecisionTree to evaluate if no matching key is found in
  *   'branches'.
  * @tparam Ctx
  *   The context type.
  * @tparam Key
  *   The type of the key extracted from the context.
  * @tparam Res
  *   The result type.
  */
case class MultiNode[Ctx, Key, Res](
    keyOf: Ctx => Key,
    branches: Map[Key, DecisionTree[Ctx, Res]],
    default: DecisionTree[Ctx, Res]
) extends DecisionTree[Ctx, Res]:
  /** Evaluates the MultiNode by determining a key from the context and
    * selecting the corresponding branch. If no specific branch matches the key,
    * the default branch is evaluated.
    * @param ctx
    *   The context object.
    * @return
    *   The result from the selected branch.
    */
  override def eval(ctx: Ctx): Res =
    branches
      .get(keyOf(ctx))
      .fold(default.eval(ctx))(_.eval(ctx))
