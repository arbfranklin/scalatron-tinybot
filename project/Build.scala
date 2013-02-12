import sbt._
import Keys._
import Project.Setting
import Keys.{ `package` => pack }

object TinyBotBuild extends Build {
  lazy val root = Project(
    id = "tinybot",
    base = file("."),
    settings = Project.defaultSettings ++ botSettings)
    
  def botSettings: Seq[Setting[_]] = Seq(
    version := "1.9-SNAPSHOT",
    organization := "com.arbfranklin.scalatron",
    
    scalaVersion := "2.9.2",
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-optimise", "-explaintypes"),
      
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2" % "1.9" % "test",
      "org.pegdown" % "pegdown" % "1.0.2" % "test",
      "junit" % "junit" % "4.7" % "test"),

    // default parameters for scalatron play goal
    scalatronDir := file("/usr/local/scalatron"),
    headless := false,
    steps := 5000,
    maxSlaves := 650,

    play <<= (scalatronDir, name, javaOptions, pack in Compile, headless, steps, maxSlaves) map {
      (base, name, javaOptions, botJar, headless, steps, maxSlaves) =>
        require(base exists, "The setting '%s' must point to the base directory of an existing Scalatron installation.".format(scalatronDir.key.label))
        IO delete (base / "bots" / name)
        IO copyFile (botJar, base / "bots" / name / "ScalatronBot.jar")

        val headlessOpts = if (headless) Seq("-maxfps", "1000", "-headless", "yes") else Seq()

        Process("java" +: (javaOptions ++ Seq("-Ddebug=true", "-jar", "Scalatron.jar", "-browser", "no",
          "-x", "100", "-y", "100", "-steps", steps.toString, "-maxslaves", maxSlaves.toString) ++ headlessOpts), base / "bin") !
    }
  )

  val headless = SettingKey[Boolean]("headless", "whether the frontend should be started, or if we should just run in headless mode")
  val maxSlaves = SettingKey[Int]("maxSlaves", "maximum number of mini-bots to allow per scalatron master")
  val steps = SettingKey[Int]("steps", "number of simulation steps to make before the apocalypse")
  val scalatronDir = SettingKey[File]("scalatron-dir", "base directory of an existing Scalatron installation")

  val play = TaskKey[Unit]("play", "recompiles, packages and installs the bot, then starts Scalatron")
}
