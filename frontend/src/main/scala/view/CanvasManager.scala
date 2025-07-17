package view

import scala.collection.mutable.ListBuffer
import com.raquo.laminar.api.L.{EventBus, Var, unsafeWindowOwner}
import model.SimulationState
import model.entities.Entity
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, html}
import update.{Event, Update}
import update.Event.UpdateWalls
import utils.Vector2D

class CanvasManager(
    model: Var[SimulationState],
    update: Var[Update],
    eventBus: EventBus[Event]
):
  private val canvas =
    dom.document.getElementById("main-canvas").asInstanceOf[html.Canvas]
  private val ctx =
    canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  val wallComponents: ListBuffer[WallComponent] =
    ListBuffer.empty[WallComponent]

  private var resizeTarget: Option[WallComponent] = None
  private var resizeStartPosition: Vector2D = Vector2D.zero
  private var originalSize: Vector2D = Vector2D.zero

  // Mouse event handlers
  canvas.onmousedown = e => handleMouseDown(e)
  canvas.onmousemove = e => handleMouseMove(e)
  canvas.onmouseup = e => handleMouseUp(e)
  canvas.onmouseleave = e => handleMouseUp(e)

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

  private def redrawAllComponents(): Unit =
    wallComponents.foreach(_.render(ctx))

  private def resizeCanvas(): Unit =
    val container = canvas.parentElement
    canvas.width = container.clientWidth
    canvas.height = container.clientHeight

  private def clearCanvas(): Unit =
    ctx.fillStyle = "#f0f0f0"
    ctx.fillRect(0, 0, canvas.width, canvas.height)

  // components part
  def addWallComponent(wall: WallComponent): Unit =
    wallComponents += wall
    val walls = (for wall <- wallComponents yield wall.model.now()).toList
    eventBus.writer.onNext(UpdateWalls(walls))
    drawComponent(wall)

  private def drawComponent[E <: Entity](component: EntityComponent[E]): Unit =
    component.render(ctx)

  // customer part
  private def drawCustomers(state: SimulationState): Unit =
    state.customers.foreach { customer =>
      ctx.beginPath()
      ctx.arc(customer.position.x, customer.position.y, 3, 0, Math.PI * 2)
      ctx.fillStyle = "green"
      ctx.fill()
      ctx.stroke()
    }

  // resize wall part
  private def handleMouseDown(e: MouseEvent): Unit =
    val mousePos = getMousePosition(e)
    wallComponents
      .find(_.contains(mousePos))
      .foreach(w => startResizing(w, mousePos))

  private def startResizing(wall: WallComponent, mousePos: Vector2D): Unit =
    resizeTarget = Some(wall)
    resizeStartPosition = mousePos
    originalSize = Vector2D(wall.model.now().width, wall.model.now().height)

  private def handleMouseMove(e: MouseEvent): Unit =
    resizeTarget.foreach { wall =>
      val mousePos = getMousePosition(e)
      updateWallSize(wall, mousePos)
      redrawAllComponents()
    }

  private def updateWallSize(wall: WallComponent, mousePos: Vector2D): Unit =
    val delta = mousePos - resizeStartPosition
    val newWidth = math.max(10, originalSize.x + delta.x)
    val newHeight = math.max(10, originalSize.y + delta.y)

    // Update the wall component with new size
    wall.resize(newWidth, newHeight)

  private def handleMouseUp(e: MouseEvent): Unit =
    if (resizeTarget.isDefined) {
      val wallIndex = wallComponents.indexOf(resizeTarget.get)
      wallComponents.remove(wallIndex)
      wallComponents.addOne(resizeTarget.get)
      val walls = (for wall <- wallComponents yield wall.model.now()).toList
      eventBus.writer.onNext(UpdateWalls(walls))
    }
    resizeTarget = None
    clearCanvas()
    redrawAllComponents()

  private def getMousePosition(e: MouseEvent): Vector2D =
    val rect = canvas.getBoundingClientRect()
    Vector2D(e.clientX - rect.left, e.clientY - rect.top)
