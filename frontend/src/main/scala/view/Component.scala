package view

import com.raquo.laminar.api.L.Var
import model.entities.{Entity, Wall}
import org.scalajs.dom
import utils.Vector2D

//case class Component(
//    x: Double,
//    y: Double,
//    componentType: String,
//    originalX: Double,
//    originalY: Double,
//    properties: Map[String, Any] = Map.empty
//)

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
