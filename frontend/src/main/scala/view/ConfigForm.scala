package view

import com.raquo.laminar.api.L.*
import com.raquo.laminar.api.L.unsafeWindowOwner
import com.raquo.laminar.nodes.ReactiveHtmlElement
import model.SimulationState
import model.entities.customers.DefaultMovementManager
import org.scalajs.dom.HTMLDivElement
import update.Update

case class ConfigForm(update: Var[Update], model: Var[SimulationState]):

  given Owner = unsafeWindowOwner
  private val maxSpeedVar = Var(1000.0)
  private val perceptionRadiusVar = Var(200000.0)
  private val avoidRadiusVar = Var(50.0)
  private val alignmentWeightVar = Var(1.0)
  private val cohesionWeightVar = Var(1.0)
  private val separationWeightVar = Var(1.0)
  private val gamesAttractivenessWeightVar = Var(1.0)
  private val sittingRadiusVar = Var(100.0)

  // Spawning strategy selection
  private val strategyTypeVar = Var("constant")
  private val constantRateVar = Var(5)
  private val gaussianPeakVar = Var(100.0)
  private val gaussianMeanVar = Var(10.0)
  private val gaussianStdDevVar = Var(2.0)
  private val stepLowRateVar = Var(2)
  private val stepHighRateVar = Var(10)
  private val stepStartVar = Var(9.0)
  private val stepEndVar = Var(17.0)

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
      hr(),
      h3("Spawning Strategy Configuration"),
      strategySelector(),
      div(
        child <-- strategyTypeVar.signal.map {
          case "constant" => constantStrategyConfig()
          case "gaussian" => gaussianStrategyConfig()
          case "step"     => stepStrategyConfig()
          case _          => div() // Custom strategy placeholder
        }
      )
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

  private def strategySelector(): HtmlElement =
    div(
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
