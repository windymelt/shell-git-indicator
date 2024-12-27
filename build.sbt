import scala.scalanative.build.LTO
import scala.scalanative.build.GC
import scala.scalanative.build.Mode
ThisBuild / scalaVersion := "3.6.2"

lazy val core = (projectMatrix in file("core"))
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "os-lib" % "0.11.3",
    ),
    nativeConfig ~= (_.withLinkingOptions(Seq("-static"))
      .withGC(GC.none)
      .withOptimize(true)
      .withMode(Mode.releaseFull)
      .withLTO(LTO.thin)),
  )
  .jvmPlatform(scalaVersions = Seq("3.6.2"))
  .nativePlatform(scalaVersions = Seq("3.6.2"))
