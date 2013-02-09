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
      
    scalatronDir := file("/usr/local/scalatron"),
    
    play <<= (scalatronDir, name, javaOptions, pack in Compile) map {
      (base, name, javaOptions, botJar) =>
        require(base exists, "The setting '%s' must point to the base directory of an existing " +
                "Scalatron installation.".format(scalatronDir.key.label))
        IO delete (base / "bots" / name)
        IO copyFile (botJar, base / "bots" / name / "ScalatronBot.jar")
        Process("java" +: (javaOptions ++ Seq("-jar", "Scalatron.jar", "-browser", "no",
          "-x", "100", "-y", "100", "-steps", "5000", "-maxslaves", "750", "-maxfps","1000")), base / "bin") !
    },
    
    testOptions := Seq(Tests.Argument("html", "console")),

    testOptions <+= crossTarget map { ct =>
      Tests.Setup { () => 
        System.setProperty("specs2.outDir", (ct / "specs2") absolutePath)
      }
    },

    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2" % "1.9" % "test",
      "org.pegdown" % "pegdown" % "1.0.2" % "test",
      "junit" % "junit" % "4.7" % "test")
  )

  val scalatronDir = SettingKey[File]("scalatron-dir", "base directory of an existing Scalatron installation")
  val play = TaskKey[Unit]("play", "recompiles, packages and installs the bot, then starts Scalatron")
}
