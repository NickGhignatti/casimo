package view

import org.scalajs.dom.DragEvent

case class Component(
    x: Double,
    y: Double,
    componentType: String,
    properties: Map[String, Any] = Map.empty
)
