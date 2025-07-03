package view

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import com.raquo.laminar.receivers.FocusReceiver.<--
import model.SimulationState
import model.customers.Customer
import org.scalajs.dom
import org.scalajs.dom.{CanvasRenderingContext2D, HTMLCanvasElement}
import update.Event.SimulationTick
import update.{Event, Update}
import utils.Vector2D

import scala.scalajs.js

class View(state: SimulationState):

  private val canvasWidth = 800
  private val canvasHeight = 500
  private val period = 100

  private val model = Var(state)
  private val eventBus = new EventBus[Event]

  // Use the stream to trigger actions, e.g., send to an EventBus
  // UPDATE LOGIC
  eventBus.events
    .scanLeft(model.now())((m, e) => Update.update(m, e))
    .foreach(model.set)(unsafeWindowOwner)

  dom.window.setInterval(
    () => eventBus.writer.onNext(Event.SimulationTick),
    period
  )

  private val stage = mainCanvas()
  private val ctx =
    stage.ref.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  def appElement(): HtmlElement =
    div(
      cls := "main-application",
      h1("Casino Simulation"),
      div(cls := "canvas-wrapper", stage),
      div(
        cls := "bottom-controls",
        button(
          "Start",
          onClick.mapTo(Event.SimulationTick) --> eventBus.writer
        ),
        button("Pause", onClick --> (_ => println("Pause clicked"))),
        button("Resume", onClick --> (_ => println("Resume clicked"))),
        button(
          "Generate",
          onClick.mapTo(Event.AddCustomers(50)) --> eventBus.writer
        )
      )
    )

  def init(): Unit =
    render(dom.document.getElementById("app"), appElement())

  def mainCanvas(): ReactiveHtmlElement[HTMLCanvasElement] =
    canvasTag(
      cls := "simulation-canvas",
      widthAttr := canvasWidth,
      heightAttr := canvasHeight,
      inContext { thisCanvas =>
        onMountCallback { _ =>
          val ctx = thisCanvas.ref
            .getContext("2d")
            .asInstanceOf[CanvasRenderingContext2D]
          model.signal.foreach { s =>
            drawCustomers(s.customers)
          }(unsafeWindowOwner)
        }
      }
    )

  private def drawCustomers(customers: Seq[Customer]): Unit =
    ctx.clearRect(0, 0, canvasWidth, canvasHeight)
    ctx.fillStyle = "#fff"
    ctx.fillRect(0, 0, canvasWidth, canvasHeight)

    customers.foreach { customer =>
      drawOval(customer.position)
    }

  def drawOval(pos: Vector2D): Unit =
    ctx.beginPath()
    ctx.arc(pos.x, pos.y, 10, 0, 2 * Math.PI)
    ctx.fillStyle = "lightblue" // azzurro chiaro
    ctx.fill()
    ctx.lineWidth = 2
    ctx.strokeStyle = "black"
    ctx.stroke()
