package view

import com.raquo.laminar.api.L.Var
import com.raquo.laminar.api.L.unsafeWindowOwner
import model.SimulationState

import scala.collection.mutable.ListBuffer
import org.scalajs.dom
import org.scalajs.dom.html

class CanvasManager(model: Var[SimulationState]):
  private val canvas =
    dom.document.getElementById("main-canvas").asInstanceOf[html.Canvas]
  private val ctx =
    canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  private val components = ListBuffer.empty[Component]

  model.signal.foreach { state =>
    clearCanvas()
    redrawAllComponents()
    drawCustomers(state)
  }(using unsafeWindowOwner)

  def init(): Unit =
    resizeCanvas()
    dom.window.addEventListener(
      "resize",
      { _ =>
        resizeCanvas()
        redrawAllComponents()
      }
    )
    clearCanvas()

  private def redrawAllComponents(): Unit = components.foreach { c =>
    drawComponent(
      c.x * (canvas.width / c.originalX),
      c.y * (canvas.height / c.originalY),
      c.componentType
    )
  }

  private def resizeCanvas(): Unit =
    val container = canvas.parentElement
    canvas.width = container.clientWidth
    canvas.height = container.clientHeight

  private def clearCanvas(): Unit =
    ctx.fillStyle = "#f0f0f0"
    ctx.fillRect(0, 0, canvas.width, canvas.height)

  def addComponent(x: Double, y: Double, componentType: String): Unit =
    components += Component(x, y, componentType, canvas.width, canvas.height)
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

  private def drawCustomers(state: SimulationState): Unit =
    state.customers.foreach { customer =>
      ctx.beginPath()
      ctx.arc(customer.position.x, customer.position.y, 3, 0, Math.PI * 2)
      ctx.fillStyle = "green"
      ctx.fill()
      ctx.stroke()
    }
