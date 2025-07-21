package view

import scala.collection.mutable.ListBuffer

import com.raquo.laminar.api.L.EventBus
import com.raquo.laminar.api.L.Var
import com.raquo.laminar.api.L.unsafeWindowOwner
import model.SimulationState
import model.entities.Entity
import org.scalajs.dom
import org.scalajs.dom.MouseEvent
import org.scalajs.dom.html
import update.Event
import update.Event.UpdateWalls
import update.Event.updateGamesList
import update.Update
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
  private val wallComponents: ListBuffer[WallComponent] =
    ListBuffer.empty[WallComponent]
  private val slotComponents: ListBuffer[SlotComponent] =
    ListBuffer.empty[SlotComponent]
  private val rouletteComponents: ListBuffer[RouletteComponent] =
    ListBuffer.empty[RouletteComponent]
  private val blackjackComponents: ListBuffer[BlackJackComponent] =
    ListBuffer.empty[BlackJackComponent]

  private val resizeTarget: Var[Option[WallComponent]] = Var(None)
  private val resizeStartPosition: Var[Vector2D] = Var(Vector2D.zero)
  private val originalSize: Var[Vector2D] = Var(Vector2D.zero)

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
    dom.window.addEventListener("load", { _ => resizeCanvas() })
    clearCanvas()

  def entityIsAlreadyPresent(point: Vector2D): Boolean =
    !wallComponents.exists(_.contains(point)) &&
      !slotComponents.exists(_.contains(point)) &&
      !rouletteComponents.exists(_.contains(point)) &&
      !blackjackComponents.exists(_.contains(point))

  private def redrawAllComponents(): Unit =
    wallComponents.foreach(_.render(ctx))
    slotComponents.foreach(_.render(ctx))
    rouletteComponents.foreach(_.render(ctx))
    blackjackComponents.foreach(_.render(ctx))

  private def resizeCanvas(): Unit =
    val container = canvas.parentElement
    canvas.width = container.clientWidth
    canvas.height = container.clientHeight
    println(canvas.width)
    println(canvas.height)

  private def clearCanvas(): Unit =
    ctx.fillStyle = "#f0f0f0"
    ctx.fillRect(0, 0, canvas.width, canvas.height)

  // components part
  def addWallComponent(wall: WallComponent): Unit =
    wallComponents += wall
    val walls = (for wall <- wallComponents yield wall.model.now()).toList
    eventBus.writer.onNext(UpdateWalls(walls))
    drawComponent(wall)

  def addSlotComponent(slot: SlotComponent): Unit =
    slotComponents += slot
    signalGameAdd()
    drawComponent(slot)

  def addRouletteComponent(roulette: RouletteComponent): Unit =
    rouletteComponents += roulette
    signalGameAdd()
    drawComponent(roulette)

  def addBlackJackComponent(blackjack: BlackJackComponent): Unit =
    blackjackComponents += blackjack
    signalGameAdd()
    drawComponent(blackjack)

  private def signalGameAdd(): Unit =
    val slots = (for slot <- slotComponents yield slot.model.now()).toList
    val roulettes =
      (for roulette <- rouletteComponents yield roulette.model.now()).toList
    val blackjacks =
      (for blackjack <- blackjackComponents yield blackjack.model.now()).toList
    eventBus.writer.onNext(updateGamesList(slots ::: roulettes ::: blackjacks))

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
    resizeTarget.update(_ => Some(wall))
    resizeStartPosition.update(_ => mousePos)
    originalSize.update(_ =>
      Vector2D(wall.model.now().width, wall.model.now().height)
    )

  private def handleMouseMove(e: MouseEvent): Unit =
    resizeTarget.now().foreach { wall =>
      val mousePos = getMousePosition(e)
      updateWallSize(wall, mousePos)
      redrawAllComponents()
    }

  private def updateWallSize(wall: WallComponent, mousePos: Vector2D): Unit =
    val delta = mousePos - resizeStartPosition.now()
    val newWidth = math.max(10, originalSize.now().x + delta.x)
    val newHeight = math.max(10, originalSize.now().y + delta.y)

    // Update the wall component with new size
    wall.resize(newWidth, newHeight)

  private def handleMouseUp(e: MouseEvent): Unit =
    if (resizeTarget.now().isDefined) {
      val wallIndex = wallComponents.indexOf(resizeTarget.now().get)
      wallComponents.remove(wallIndex)
      wallComponents.addOne(resizeTarget.now().get)
      val walls = (for wall <- wallComponents yield wall.model.now()).toList
      eventBus.writer.onNext(UpdateWalls(walls))
    }
    resizeTarget.update(_ => None)
    clearCanvas()
    redrawAllComponents()

  private def getMousePosition(e: MouseEvent): Vector2D =
    val rect = canvas.getBoundingClientRect()
    Vector2D(e.clientX - rect.left, e.clientY - rect.top)
