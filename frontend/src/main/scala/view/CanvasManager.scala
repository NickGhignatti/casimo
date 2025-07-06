package view

import org.scalajs.dom
import org.scalajs.dom.html

import scala.collection.mutable.ListBuffer

class CanvasManager:
  private val canvas =
    dom.document.getElementById("main-canvas").asInstanceOf[html.Canvas]
  private val ctx =
    canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  private val components = ListBuffer.empty[Component]

  def init(): Unit =
    resizeCanvas()
    clearCanvas()
    DragDrop.registerCanvasManager(this)

  private def resizeCanvas(): Unit =
    val container = canvas.parentElement
    components.foreach(c =>
      drawComponent(
        c.x,
        c.y,
        c.componentType
      )
    )

    canvas.width = container.clientWidth
    canvas.height = container.clientHeight

  private def clearCanvas(): Unit =
    ctx.fillStyle = "#f0f0f0"
    ctx.fillRect(0, 0, canvas.width, canvas.height)

  def addComponent(x: Double, y: Double, componentType: String): Unit =
    components += Component(x, y, componentType)
    drawComponent(x, y, componentType)

  private def drawComponent(x: Double, y: Double, componentType: String): Unit =
    ctx.beginPath()
    ctx.arc(x, y, 25, 0, Math.PI * 2)
    ctx.fillStyle = "#3498db"
    ctx.fill()
    ctx.stroke()

    ctx.fillStyle = "white"
    ctx.textAlign = "center"
    ctx.textBaseline = "middle"
    ctx.fillText(componentType.take(2), x, y)
