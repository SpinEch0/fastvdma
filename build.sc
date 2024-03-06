import $ivy.`com.goyeau::mill-scalafix_mill0.11:0.3.1`
import com.goyeau.mill.scalafix.ScalafixModule
import coursier.maven.MavenRepository

import mill._
import scalalib._
import scalafmt._
import $file.common

def defaultVersions(chiselVersion: String) = chiselVersion match {
  case "chisel" => Map(
    "chisel"        -> ivy"org.chipsalliance::chisel:6.1.0",
    "chisel-plugin" -> ivy"org.chipsalliance:::chisel-plugin:6.1.0",
    "play-json"     -> ivy"com.typesafe.play::play-json:2.8.+",
    "iotesters"     -> ivy"edu.berkeley.cs::chisel-iotesters:2.5.5",
    "chiseltest"    -> ivy"edu.berkeley.cs::chiseltest:6.0-SNAPSHOT"
  )
  case "chisel3" => Map(
    "chisel"        -> ivy"edu.berkeley.cs::chisel3:3.5.3",
    "chisel-plugin" -> ivy"edu.berkeley.cs:::chisel3-plugin:3.5.3",
    "play-json"     -> ivy"com.typesafe.play::play-json:2.8.+",
    "iotesters"     -> ivy"edu.berkeley.cs::chisel-iotesters:2.5.5",
    "chiseltest"    -> ivy"edu.berkeley.cs::chiseltest:0.5.0"
  )
}

trait HasChisel extends SbtModule with Cross.Module[String] {


  def repositoriesTask = T.task {
    super.repositoriesTask() ++ Seq(MavenRepository("https://oss.sonatype.org/content/repositories/snapshots"))
  }

  def chiselModule: Option[ScalaModule] = None

  def chiselPluginJar: T[Option[PathRef]] = None

  def chiselIvy: Option[Dep] = Some(defaultVersions(crossValue)("chisel"))

  def chiselPluginIvy: Option[Dep] = Some(defaultVersions(crossValue)("chisel-plugin"))

  def playJsonIvy: Option[Dep] = Some(defaultVersions(crossValue)("play-json"))

  override def scalaVersion = if (crossValue == "chisel") {
      "2.13.12"
  } else {
      "2.12.13"
  }

  override def scalacOptions = super.scalacOptions() ++
    Agg("-language:reflectiveCalls", "-Ywarn-unused")

  override def ivyDeps = super.ivyDeps() ++ Agg(chiselIvy.get) ++ Agg(playJsonIvy.get)

  override def scalacPluginIvyDeps = super.scalacPluginIvyDeps() ++ Agg(chiselPluginIvy.get)
}

object fvdma extends Cross[FVDMA]("chisel", "chisel3")
trait FVDMA extends millbuild.common.FVDMAModule
    with HasChisel
    with ScalafixModule
    with ScalafmtModule {

  override def millSourcePath = os.pwd

  override def forkArgs = Seq("-Xmx8G", "-Xss256m")

  override def sources = T.sources {
    super.sources() ++ Seq(PathRef(this.millSourcePath / "src" / crossValue / "main" / "scala"))
  }

  object test extends SbtModuleTests
      with TestModule.ScalaTest with ScalafixModule
      with ScalafmtModule {

    override def forkArgs = Seq("-Xmx8G", "-Xss256m")

    override def sources = T.sources {
      super.sources() ++ Seq(PathRef(this.millSourcePath / "src" / crossValue / "test" / "scala"))
    }

    override def ivyDeps = super.ivyDeps() ++ Agg(
      defaultVersions(crossValue)("chiseltest")
    ) ++ Agg(
      defaultVersions(crossValue)("iotesters")
    )
  }
}
