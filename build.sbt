import sbt.Keys.libraryDependencies
import org.scalajs.linker.interface.ModuleSplitStyle

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

lazy val installHooksIfNeeded = taskKey[Unit]("Install Git hooks if not yet installed")

installHooksIfNeeded := Def.taskDyn {
  if (!hookInstalledFlag.exists()) {
    Def.task {
      val log = streams.value.log
      log.info("Git hooks not found — running installGitHooks...")
      installGitHooks.value
      IO.write(hookInstalledFlag, "installed")
      log.success("Git hooks installation complete.")
    }
  } else {
    Def.task {
      //streams.value.log.info("Git hooks already installed; skipping.")
    }
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

lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "casimo",
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("casimo"))
        )
    },
    libraryDependencies ++= Seq(
      // Test dependencies
      "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
      "org.scalatest" %% "scalatest" % "3.2.18" % Test,
      "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test,
      // ScalaJs dependencies
      "org.scalameta" %%% "munit" % "1.1.1" % Test,
      "org.scala-js" %%% "scalajs-dom" % "2.8.0",
      "org.scala-js" %%% "scalajs-dom" % "2.8.0",
      "com.raquo" %%% "laminar" % "17.0.0"
    )
  )
