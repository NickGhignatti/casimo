package view

import com.raquo.laminar.api.L._
import com.raquo.laminar.api.L.unsafeWindowOwner
import com.raquo.laminar.nodes.ReactiveHtmlElement
import model.entities.customers.DefaultMovementManager
import org.scalajs.dom.HTMLDivElement
import update.Update

case class ConfigForm(update: Var[Update]):

  given Owner = unsafeWindowOwner
  private val maxSpeedVar = Var(1000.0)
  private val perceptionRadiusVar = Var(200000.0)
  private val avoidRadiusVar = Var(50.0)
  private val alignmentWeightVar = Var(1.0)
  private val cohesionWeightVar = Var(1.0)
  private val separationWeightVar = Var(1.0)
  private val gamesAttractivenessWeightVar = Var(1.0)
  private val sittingRadiusVar = Var(100.0)

  Signal
    .combineWithFn(
      maxSpeedVar,
      perceptionRadiusVar,
      avoidRadiusVar,
      alignmentWeightVar,
      cohesionWeightVar,
      separationWeightVar,
      gamesAttractivenessWeightVar,
      sittingRadiusVar
    )(
      DefaultMovementManager(_, _, _, _, _, _, _, _)
    )
    .map(Update(_))
    .foreach(update.set)

  def init(): ReactiveHtmlElement[HTMLDivElement] =
    div(
      h3("Movement Manager Form"),
      parameter(
        "Max Speed",
        maxSpeedVar
      ),
      parameter(
        "Perception Radius",
        perceptionRadiusVar
      ),
      parameter(
        "Avoid Radius",
        avoidRadiusVar
      ),
      parameter(
        "Alignment Weight",
        alignmentWeightVar
      ),
      parameter(
        "Cohesion Weight",
        cohesionWeightVar
      ),
      parameter(
        "Separation Weight",
        separationWeightVar
      ),
      parameter(
        "Games Attractiveness Weight",
        gamesAttractivenessWeightVar
      ),
      parameter(
        "Sitting Radius",
        sittingRadiusVar
      ),
      hr()
    )

  private def parameter(
      labelText: String,
      variable: Var[Double]
  ): HtmlElement =
    div(
      label(labelText),
      input(
        typ := "number",
        onInput.mapToValue.map(_.toDoubleOption.getOrElse(0.0)) --> variable,
        value := variable.now().toString
      )
    )
