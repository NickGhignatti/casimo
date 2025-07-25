package utils

sealed trait DecisionTree[Ctx, Res]:
  def eval(ctx: Ctx): Res

case class Leaf[Ctx, Res](action: Ctx => Res) extends DecisionTree[Ctx, Res]:
  override def eval(ctx: Ctx): Res = action(ctx)

case class DecisionNode[Ctx, Res](
    predicate: Ctx => Boolean,
    trueBranch: DecisionTree[Ctx, Res],
    falseBranch: DecisionTree[Ctx, Res]
) extends DecisionTree[Ctx, Res]:
  override def eval(ctx: Ctx): Res =
    if predicate(ctx) then trueBranch.eval(ctx)
    else falseBranch.eval(ctx)

case class MultiNode[Ctx, Key, Res](
    keyOf: Ctx => Key,
    branches: Map[Key, DecisionTree[Ctx, Res]],
    default: DecisionTree[Ctx, Res]
) extends DecisionTree[Ctx, Res]:
  override def eval(ctx: Ctx): Res =
    branches
      .get(keyOf(ctx))
      .fold(default.eval(ctx))(_.eval(ctx))
