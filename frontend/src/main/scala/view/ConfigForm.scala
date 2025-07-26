package view

import com.raquo.laminar.api.L._
import com.raquo.laminar.api.L.unsafeWindowOwner
import com.raquo.laminar.nodes.ReactiveHtmlElement
import model.SimulationState
import model.entities.customers.DefaultMovementManager
import org.scalajs.dom.HTMLDivElement
import update.Update

case class ConfigForm(update: Var[Update], model: Var[SimulationState]):

  given Owner = unsafeWindowOwner
  private case class Parameter(
      label: String,
      variable: Var[Double],
      updater: (DefaultMovementManager, Double) => DefaultMovementManager
  )
  private val parameters = List(
    Parameter("Max Speed", Var(20.0), (m, v) => m.copy(maxSpeed = v)),
    Parameter(
      "Perception Radius",
      Var(100.0),
      (m, v) => m.copy(perceptionRadius = v)
    ),
    Parameter("Avoid Radius", Var(50.0), (m, v) => m.copy(avoidRadius = v)),
    Parameter(
      "Alignment Weight",
      Var(0.1),
      (m, v) => m.copy(alignmentWeight = v)
    ),
    Parameter(
      "Cohesion Weight",
      Var(0.1),
      (m, v) => m.copy(cohesionWeight = v)
    ),
    Parameter(
      "Separation Weight",
      Var(0.1),
      (m, v) => m.copy(separationWeight = v)
    ),
    Parameter(
      "Games Attractiveness Weight",
      Var(1.0),
      (m, v) => m.copy(gamesAttractivenessWeight = v)
    ),
    Parameter(
      "Sitting Radius",
      Var(30.0),
      (m, v) => m.copy(sittingRadius = v)
    ),
    Parameter(
      "Boredom increase",
      Var(1),
      (m, v) => m.copy(boredomIncrease = v)
    ),
    Parameter(
      "Random movement weight",
      Var(0.2),
      (m, v) => m.copy(randomMovementWeight = v)
    )
  )
  // Spawning strategy selection
  private val strategyTypeVar = Var("step")
  private val constantRateVar = Var(1)
  private val gaussianPeakVar = Var(100.0)
  private val gaussianMeanVar = Var(10.0)
  private val gaussianStdDevVar = Var(2.0)
  private val stepLowRateVar = Var(1)
  private val stepHighRateVar = Var(5)
  private val stepStartVar = Var(21.0)
  private val stepEndVar = Var(24.0)

  def currentStrategyType: String = strategyTypeVar.now()
  def constantStrategyConfigInfo: Int = constantRateVar.now()
  def gaussianStrategyConfigInfo: (Double, Double, Double) =
    (gaussianPeakVar.now(), gaussianMeanVar.now(), gaussianStdDevVar.now())
  def stepStrategyConfigInfo: (Double, Double, Double, Double) =
    (
      stepLowRateVar.now(),
      stepHighRateVar.now(),
      stepStartVar.now(),
      stepEndVar.now()
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
      div(
        className := "config-container",
        // Column 1: Movement Manager
        div(
          className := "movement-manager-section",
          h3(
            className := "section-header",
            "Movement Manager"
          ),
          parameters.map { case Parameter(label, variable, _) =>
            parameter(label, variable)
          }
        ),
        // Column 2: Spawner Configuration
        div(
          className := "spawner-section",
          h3(className := "section-header", "Spawner Configuration"),
          strategySelector(),
          div(
            className := "strategy-params",
            child <-- strategyTypeVar.signal.map {
              case "constant" => constantStrategyConfig()
              case "gaussian" => gaussianStrategyConfig()
              case "step"     => stepStrategyConfig()
              case _ =>
                div(
                  className := "custom-placeholder",
                  "Custom strategy configuration coming soon!"
                )
            }
          )
        )
      )
    )

  private def parameter(
      labelText: String,
      variable: Var[Double]
  ): HtmlElement =
    div(
      className := "parameter-row",
      label(labelText),
      input(
        typ := "number",
        onInput.mapToValue.map(_.toDoubleOption.getOrElse(0.0)) --> variable,
        value := variable.now().toString
      )
    )

  private def strategySelector(): HtmlElement =
    div(
      className := "strategy-selector",
      label("Strategy Type"),
      select(
        value <-- strategyTypeVar,
        onChange.mapToValue --> strategyTypeVar,
        option(value := "constant", "Constant"),
        option(value := "gaussian", "Gaussian"),
        option(value := "step", "Step"),
        option(value := "custom", "Custom (Coming Soon)")
      )
    )

  private def constantStrategyConfig(): HtmlElement =
    div(
      parameterInt("Customer Rate", constantRateVar)
    )

  private def gaussianStrategyConfig(): HtmlElement =
    div(
      parameterDouble("Peak Value", gaussianPeakVar),
      parameterDouble("Mean Time", gaussianMeanVar),
      parameterDouble("Std Deviation", gaussianStdDevVar)
    )

  private def stepStrategyConfig(): HtmlElement =
    div(
      parameterInt("Low Rate", stepLowRateVar),
      parameterInt("High Rate", stepHighRateVar),
      parameterDouble("Start Time", stepStartVar),
      parameterDouble("End Time", stepEndVar)
    )

  private def parameterInt(
      labelText: String,
      variable: Var[Int]
  ): HtmlElement =
    div(
      label(labelText),
      input(
        `type` := "number",
        onInput.mapToValue.map(_.toIntOption.getOrElse(0)) --> variable,
        value := variable.now().toString
      )
    )

  private def parameterDouble(
      labelText: String,
      variable: Var[Double]
  ): HtmlElement =
    parameter(labelText, variable)
