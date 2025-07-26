package view

import com.raquo.laminar.api.L.Var
import model.entities.Entity
import model.entities.Wall
import model.entities.games.BlackJackGame
import model.entities.games.RouletteGame
import model.entities.games.SlotMachineGame
import org.scalajs.dom
import utils.Vector2D

trait EntityComponent[E <: Entity]:
  val model: Var[E]
  def render(ctx: dom.CanvasRenderingContext2D): Unit
  def contains(point: Vector2D): Boolean

class WallComponent(initialModel: Wall) extends EntityComponent[Wall]:
  override val model: Var[Wall] = Var(initialModel)

  def render(ctx: dom.CanvasRenderingContext2D): Unit =
    ctx.fillStyle = "#3498db"
    ctx.fillRect(
      model.now().position.x,
      model.now().position.y,
      model.now().width,
      model.now().height
    )

    // Draw border
    ctx.strokeStyle = "#2980b9"
    ctx.lineWidth = 2
    ctx.strokeRect(
      model.now().position.x,
      model.now().position.y,
      model.now().width,
      model.now().height
    )

  def contains(point: Vector2D): Boolean =
    model.now().contains(point)

  def resize(width: Double, height: Double): Unit =
    model.update(wall => wall.withSize(width, height))

class SlotComponent(initialModel: SlotMachineGame)
    extends EntityComponent[SlotMachineGame]:
  override val model: Var[SlotMachineGame] = Var(initialModel)

  def render(ctx: dom.CanvasRenderingContext2D): Unit =
    ctx.fillStyle = "#910909"
    val modelComponent = model.now()
    ctx.fillRect(
      modelComponent.position.x,
      modelComponent.position.y,
      modelComponent.width,
      modelComponent.height
    )

    // Draw border
    ctx.strokeStyle = "#910909"
    ctx.lineWidth = 2
    ctx.strokeRect(
      modelComponent.position.x,
      modelComponent.position.y,
      modelComponent.width,
      modelComponent.height
    )

  def contains(point: Vector2D): Boolean =
    model.now().contains(point)

class RouletteComponent(initialModel: RouletteGame)
    extends EntityComponent[RouletteGame]:
  override val model: Var[RouletteGame] = Var(initialModel)

  def render(ctx: dom.CanvasRenderingContext2D): Unit =
    ctx.fillStyle = "#aa16ba"
    val modelComponent = model.now()
    ctx.fillRect(
      modelComponent.position.x,
      modelComponent.position.y,
      modelComponent.width,
      modelComponent.height
    )

    // Draw border
    ctx.strokeStyle = "#aa16ba"
    ctx.lineWidth = 2
    ctx.strokeRect(
      modelComponent.position.x,
      modelComponent.position.y,
      modelComponent.width,
      modelComponent.height
    )

  def contains(point: Vector2D): Boolean =
    model.now().contains(point)

class BlackJackComponent(initialModel: BlackJackGame)
    extends EntityComponent[BlackJackGame]:
  override val model: Var[BlackJackGame] = Var(initialModel)

  def render(ctx: dom.CanvasRenderingContext2D): Unit =
    ctx.fillStyle = "#24ba16"
    val modelComponent = model.now()
    ctx.fillRect(
      modelComponent.position.x,
      modelComponent.position.y,
      modelComponent.width,
      modelComponent.height
    )

    // Draw border
    ctx.strokeStyle = "#24ba16"
    ctx.lineWidth = 2
    ctx.strokeRect(
      modelComponent.position.x,
      modelComponent.position.y,
      modelComponent.width,
      modelComponent.height
    )

  def contains(point: Vector2D): Boolean =
    model.now().contains(point)
