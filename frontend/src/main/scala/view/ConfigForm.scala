package view

import com.raquo.laminar.api.L._
import com.raquo.laminar.api.L.unsafeWindowOwner
import com.raquo.laminar.nodes.ReactiveHtmlElement
import model.entities.customers.DefaultMovementManager
import org.scalajs.dom.HTMLDivElement
import update.Update

case class ConfigForm(update: Var[Update]):

  given Owner = unsafeWindowOwner
  private case class Parameter(
      label: String,
      variable: Var[Double],
      updater: (DefaultMovementManager, Double) => DefaultMovementManager
  )
  private val parameters = List(
    Parameter("Max Speed", Var(1000.0), (m, v) => m.copy(maxSpeed = v)),
    Parameter(
      "Perception Radius",
      Var(100.0),
      (m, v) => m.copy(perceptionRadius = v)
    ),
    Parameter("Avoid Radius",
      Var(50.0),
      (m, v) => m.copy(avoidRadius = v)),
    Parameter(
      "Alignment Weight",
      Var(1.0),
      (m, v) => m.copy(alignmentWeight = v)
    ),
    Parameter(
      "Cohesion Weight",
      Var(1.0),
      (m, v) => m.copy(cohesionWeight = v)
    ),
    Parameter(
      "Separation Weight",
      Var(1.0),
      (m, v) => m.copy(separationWeight = v)
    ),
    Parameter(
      "Games Attractiveness Weight",
      Var(1.0),
      (m, v) => m.copy(gamesAttractivenessWeight = v)
    ),
    Parameter(
      "Sitting Radius",
      Var(100.0),
      (m, v) => m.copy(sittingRadius = v)
    ),
    Parameter(
      "Avoid Walls Weight",
      Var(5.0),
      (m, v) => m.copy(avoidWallsWeight = v)
    ),
    Parameter(
      "Avoid Walls Perception Size",
      Var(100.0),
      (m, v) => m.copy(avoidWallsPerceptionSize = v)
    )
  )
  parameters
    .map { case Parameter(_, variable, updater) => (variable.signal, updater) }
    .foreach { case (variable, updater) =>
      variable
        .map(updater(update.now().customerManager, _))
        .foreach(newManager =>
          update.set(update.now().copy(customerManager = newManager))
        )
    }

  def init(): ReactiveHtmlElement[HTMLDivElement] =
    div(
      h3("Movement Manager Form"),
      parameters.map { case Parameter(label, variable, _) =>
        parameter(label, variable)
      },
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
