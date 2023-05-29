scalaVersion := "2.13.8"
name := "hello-world"
organization := "com.graylog.plugin"
version := "1.0"


libraryDependencies += "org.graylog2" % "graylog2-server" % "5.1.1" % "provided"



assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs@_*) =>
    xs.map(_.toLowerCase) match {
      case ("manifest.mf" :: Nil) | ("notice.txt" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ("license.txt" :: Nil) =>
        MergeStrategy.discard
      case ps@(x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "maven" :: xs =>
        MergeStrategy.discard
      case "pom.properties" :: xs =>
        MergeStrategy.discard
      case "pom.xml" :: Nil =>
        MergeStrategy.discard
      case "plexus" :: Nil =>
        MergeStrategy.discard
      case "io.netty.versions.properties" :: xs =>
        MergeStrategy.first
      case ("license" :: Nil) | ("notice" :: Nil) =>
        MergeStrategy.first
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) | ("spring.tooling" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.deduplicate
    }
  case _ => MergeStrategy.first
}

assembly / assemblyJarName := s"${name.value}-${version.value}.jar"
