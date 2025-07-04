//package view
//
//import com.raquo.laminar.api.L.*
//import com.raquo.laminar.nodes.ReactiveHtmlElement
//import model.SimulationState
//import org.scalajs.dom
//import org.scalajs.dom.{CanvasRenderingContext2D, HTMLCanvasElement, document}
//import update.{Event, Update}
//import utils.Vector2D
//
//import scala.scalajs.js
//import scala.scalajs.js.annotation.JSGlobal
//
//class View(state: SimulationState):
//
//  private val canvasWidth = 800
//  private val canvasHeight = 500
//
//  private val model = Var(state)
//  private val eventBus = new EventBus[Event]
//
//  // UPDATE LOGIC
//  eventBus.events
//    .scanLeft(model.now())((m, e) => Update.update(m, e))
//    .foreach(model.set)(using unsafeWindowOwner) // fixed implicit passing
//
//  private val stage = mainCanvas()
//  private val ctx: CanvasRenderingContext2D =
//    getCanvasContext(stage.ref)
//
//  def appElement(): HtmlElement =
//    div(
//      cls := "main-application",
//      h1("Casino Simulation"),
//      div(cls := "canvas-wrapper", stage),
//      div(
//        cls := "bottom-controls",
//        button(
//          "Start",
//          onClick.mapTo(Event.SimulationTick) --> eventBus.writer
//        ),
//        button("Pause", onClick --> (_ => println("Pause clicked"))),
//        button("Resume", onClick --> (_ => println("Resume clicked"))),
//        button(
//          "Generate",
//          onClick.mapTo(Event.AddCustomers(50)) --> eventBus.writer
//        )
//      )
//    )
//
//  def init(): Unit =
//    render(document.getElementById("app"), appElement())
//
//  def mainCanvas(): ReactiveHtmlElement[HTMLCanvasElement] =
//    canvasTag(
//      cls := "simulation-canvas",
//      widthAttr := canvasWidth,
//      heightAttr := canvasHeight,
//      inContext { thisCanvas =>
//        onMountCallback { _ =>
//          val ctx = getCanvasContext(thisCanvas.ref)
//          model.signal.foreach { s =>
//            drawCustomers(s, ctx)
//          }(using unsafeWindowOwner)
//        }
//      }
//    )
//
//  private def drawCustomers(
//      state: SimulationState,
//      ctx: CanvasRenderingContext2D
//  ): Unit =
//    ctx.clearRect(0, 0, canvasWidth, canvasHeight)
//    ctx.fillStyle = "#fff"
//    ctx.fillRect(0, 0, canvasWidth, canvasHeight)
//
//    state.customers.foreach { customer =>
//      drawOval(customer.pos, ctx)
//    }
//
//def drawOval(pos: Vector2D, ctx: CanvasRenderingContext2D): Unit =
//  safeCtx(ctx).beginPath()
//  safeCtx(ctx).arc(pos.x, pos.y, 10, 0, 2 * Math.PI)
//  ctx.fillStyle = "lightblue" // light blue fill style
//  safeCtx(ctx).fill()
//  ctx.lineWidth = 2
//  ctx.strokeStyle = "black"
//  safeCtx(ctx).stroke()
//
//// Helper facade without default params to avoid Scala.js IR crash
//@js.native
//private trait SafeCanvasRenderingContext2D extends js.Object:
//  def fill(): Unit = js.native
//  def beginPath(): Unit = js.native
//  def arc(
//      x: Double,
//      y: Double,
//      radius: Double,
//      startAngle: Double,
//      endAngle: Double
//  ): Unit = js.native
//  def clearRect(x: Double, y: Double, w: Double, h: Double): Unit = js.native
//  def fillRect(x: Double, y: Double, w: Double, h: Double): Unit = js.native
//  def stroke(): Unit = js.native
//
//private def safeCtx(
//    ctx: CanvasRenderingContext2D
//): SafeCanvasRenderingContext2D =
//  ctx.asInstanceOf[SafeCanvasRenderingContext2D]
//
//private def getCanvasContext(
//    canvas: HTMLCanvasElement
//): CanvasRenderingContext2D =
//  canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
