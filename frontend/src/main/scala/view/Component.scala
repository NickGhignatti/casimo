package view

import org.scalajs.dom.DragEvent

case class Component(
    x: Double,
    y: Double,
    componentType: String,
    properties: Map[String, Any] = Map.empty
)

object Component:
  def fromDragEvent(e: DragEvent): Component =
    val canvas = e.target.asInstanceOf[org.scalajs.dom.HTMLElement]
    val rect = canvas.getBoundingClientRect()
    Component(
      x = e.clientX - rect.left,
      y = e.clientY - rect.top,
      componentType = e.dataTransfer.getData("text/plain")
    )
