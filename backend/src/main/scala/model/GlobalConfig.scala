package model

case class GlobalConfig(
    customMovementDef: Map[
      String,
      Any /*MovementStrategy*/
    ], // Or just movement params
    customStrategyDef: Map[String, Any /*CustomerGameStrategy*/ ]
    // Other static params that are configured at the start that we could need
)
