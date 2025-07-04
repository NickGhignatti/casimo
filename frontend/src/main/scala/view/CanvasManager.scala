package view

import org.scalajs.dom
import org.scalajs.dom.html

class CanvasManager:
  private val canvas =
    dom.document.getElementById("main-canvas").asInstanceOf[html.Canvas]
  private val ctx =
    canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  def init(): Unit =
    resizeCanvas()
    dom.window.addEventListener("resize", { _ => resizeCanvas() })
    clearCanvas()
    DragDrop.registerCanvasManager(this)

  private def resizeCanvas(): Unit =
    val container = canvas.parentElement
    canvas.width = container.clientWidth
    canvas.height = container.clientHeight

  def clearCanvas(): Unit =
    ctx.fillStyle = "#f0f0f0"
    ctx.fillRect(0, 0, canvas.width, canvas.height)

  def drawComponent(x: Double, y: Double, componentType: String): Unit =
    ctx.beginPath()
    ctx.arc(x, y, 25, 0, Math.PI * 2)
    ctx.fillStyle = "#3498db"
    ctx.fill()
    ctx.stroke()

    ctx.fillStyle = "white"
    ctx.textAlign = "center"
    ctx.textBaseline = "middle"
    ctx.fillText(componentType.take(2), x, y)
