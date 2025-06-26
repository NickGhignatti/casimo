package view

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import model.SimulationState
import model.entities.customers.{Customer, CustomerID}
import org.scalajs.dom
import org.scalajs.dom.HTMLCanvasElement
import update.Update

import scala.:+
import scala.scalajs.js
import scala.util.Random

def InitView(): Unit =
  renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    View.appElement()
  )
object View:
  val stage = mainCanvas()

  val viewModel = ViewModel()
  import viewModel.*

  def appElement(): Element =
    div(
      cls := "main-application",
      height := "98vh",
      width := "99vw",
      div(
        cls := "canvas-wrapper",
        stage
      ),
      controls(),
      table(
        thead("ciao"),
        tbody(
//          children <-- dataSignal.map(data => data.map { item =>
//            renderDataItem(item.id, item)
//          })
        )
      )
    )

  def controls(): Element =
    div(
      cls := "bottom-controls",
      button(
        "Start",
        onClick --> { _ =>
          Update.update(
            SimulationState(List(), List()),
            update.Event.SimulationTick
          )
        }
      ),
      button("Pause", onClick --> { _ => println("Pause clicked") }),
      button("Resume", onClick --> { _ => println("Resume clicked") }),
      button(
        "Generate",
        onClick --> { _ =>
          println("Generate customers")
          generateCustomer()
          dataSignal.map(data => data.map(c => renderCustomer(c.id, c)))
        }
      )
    )

  def renderDataItem(id: CustomerID, item: Customer): Element =
    tr(
      td(item.x),
      td(item.y)
    )

  def renderCustomer(id: CustomerID, item: Customer): Unit =
    val canvas = stage.ref
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.fillStyle = "#fff"
    ctx.fillRect(0, 0, canvas.width, canvas.height)

    ctx.beginPath()
    ctx.arc(item.x, item.y, 5.0, 0, 2 * Math.PI)
    ctx.fillStyle = "#007acc"
    ctx.fill()

  def mainCanvas(): ReactiveHtmlElement[HTMLCanvasElement] =
    canvasTag(
      cls := "simulation-canvas",
      widthAttr := 800,
      heightAttr := 300,
      onMountCallback { ctx =>
        val canvas = ctx.thisNode.ref
        drawInitial(canvas)
      }
    )

  def generateCustomer(): Unit =
    (1 to 50).foreach { _ =>
      val newCustomer = Customer(
        x = Random.between(10.0, 790.0),
        y = Random.between(10.0, 290.0)
      )
      addCustomer(newCustomer)
    }

  private def drawInitial(canvas: dom.html.Canvas): Unit =
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    ctx.fillStyle = "#fff"
    ctx.fillRect(0, 0, canvas.width, canvas.height)

  private def drawCustomers(customers: Signal[List[Customer]]): Unit =
    val canvas = stage.ref
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.fillStyle = "#fff"
    ctx.fillRect(0, 0, canvas.width, canvas.height)

    customers.map(_.foreach { c =>
      ctx.beginPath()
      ctx.arc(c.x, c.y, 5.0, 0, 2 * Math.PI)
      ctx.fillStyle = "#007acc"
      ctx.fill()
      println(s"customer printed at: ${c.x} ${c.y}")
    })
