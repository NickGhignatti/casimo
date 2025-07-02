import sbt.Keys.libraryDependencies
import org.scalajs.linker.interface.{ModuleSplitStyle, OutputPatterns}
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import scalajscrossproject.ScalaJSCrossPlugin.autoImport.*

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

// Define a custom task to install Git hooks
lazy val installGitHooks =
  taskKey[Unit]("Install Git hooks from the .githooks directory")

installGitHooks := {
  val log = streams.value.log
  val gitHooksDir =
    baseDirectory.value / ".githooks" // Directory containing your custom hooks
  val targetDir =
    baseDirectory.value / ".git" / "hooks" // Git's hooks directory

  if (!targetDir.exists()) {
    log.error(
      "[ERROR] .git/hooks directory not found. Is this a Git repository?"
    )
  } else {
    val hooks = (gitHooksDir * "*").get // Retrieve all files in .githooks
    hooks.foreach { hook =>
      val target = targetDir / hook.getName
      IO.copyFile(hook, target) // Copy each hook to .git/hooks
      target.setExecutable(true) // Ensure the hook is executable
    }
    log.success(s"[OK] Git hooks installed from ${gitHooksDir.getPath}")
  }
}

// Define a flag file to indicate hooks have been installed
val hookInstalledFlag = file(".git/hooks/.hooks-installed")

lazy val installHooksIfNeeded =
  taskKey[Unit]("Install Git hooks if not yet installed")

installHooksIfNeeded := Def.taskDyn {
  if (!hookInstalledFlag.exists()) {
    Def.task {
      val log = streams.value.log
      log.info("Git hooks not found — running installGitHooks...")
      installGitHooks.value
      IO.write(hookInstalledFlag, "installed")
      log.success("Git hooks installation complete.")
    }
  } else
    Def.task {
      // streams.value.log.info("Git hooks already installed; skipping.")
    }
}.value

// Automatically run the installHooksIfNeeded task when SBT starts
Global / onLoad := {
  val previous = (Global / onLoad).value
  state =>
    val extracted = Project.extract(state)
    val (newState, _) = extracted.runTask(installHooksIfNeeded, state)
    previous(newState)
}

// Define a task to reset the hooks installation flag
lazy val resetHooks = taskKey[Unit]("Remove the Git hooks installation flag")

resetHooks := {
  if (hookInstalledFlag.exists()) {
    IO.delete(hookInstalledFlag)
    println(
      "[CLEANUP] Git hooks installation flag removed. Hooks will be reinstalled on next SBT startup."
    )
  } else
    println("[INFO] No installation flag found. No action needed.")
}

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .in(file("shared"))
  .settings(
    name := "casimo-shared",
    scalaVersion := "3.3.5"
  )

// Backend-specific implementation (JVM only)
lazy val backend = project
  .in(file("backend"))
  .settings(
    name := "casimo-backend",
    scalaVersion := "3.3.5",
    coverageEnabled := true,
    // Backend source locations
    Compile / scalaSource := baseDirectory.value / "src" / "main" / "scala",
    Test / scalaSource := baseDirectory.value / "src" / "test" / "scala",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test
    )
  )
  .dependsOn(shared.jvm)

// Frontend-specific implementation (JS only)
lazy val frontend = project
  .in(file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "casimo-frontend",
    scalaVersion := "3.3.5",
    coverageEnabled := false,
    // Frontend source locations
    Compile / scalaSource := baseDirectory.value / "src" / "main" / "scala",
    Test / scalaSource := baseDirectory.value / "src" / "test" / "scala",
    scalaJSUseMainModuleInitializer := true,
    moduleName := "casimo",
    Compile / fullLinkJS / scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.CommonJSModule)
        .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("casimo")))
        .withCheckIR(false)
    },
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.8.0",
      "com.raquo" %%% "laminar" % "17.0.0"
    )
  )
  .dependsOn(shared.js, backend)

// Root project aggregates all modules
lazy val root = project
  .in(file("."))
  .aggregate(backend, frontend, shared.jvm, shared.js)
  .settings(
    publish := {},
    publishLocal := {},
    coverageExcludedPackages := ".*", // Exclude everything in root
    coverageEnabled := false
  )

// command sbt "coverage; backend/test; coverageReport"
