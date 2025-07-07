package view

import org.scalajs.dom.DragEvent

case class Component(
    x: Double,
    y: Double,
    componentType: String,
    originalX: Double,
    originalY: Double,
    properties: Map[String, Any] = Map.empty
)
