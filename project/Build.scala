import sbt._
import Keys._

import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

object ApplicationBuild extends Build {

  val appName = "akka-raft"
  val appVersion = "1.0-SNAPSHOT"

  import Dependencies._

  val debugInUse = SettingKey[Boolean]("debug-in-use", "debug is used")

  lazy val akkaRaft = Project(appName, file("."))
    .configs(MultiJvm)
    .settings(multiJvmSettings: _*)
    .settings(
      libraryDependencies ++= generalDependencies,
      ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
    )

  lazy val multiJvmSettings = SbtMultiJvm.multiJvmSettings ++ Seq(
     // make sure that MultiJvm test are compiled by the default test compilation
     compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
     // disable parallel tests
     parallelExecution in Test := false,
     // make sure that MultiJvm tests are executed by the default test target
     executeTests in Test <<=
       ((executeTests in Test), (executeTests in MultiJvm)).map{
         case (outputOfTests, outputOfMultiJVMTests)  =>
           Tests.Output(Seq(outputOfTests.overall, outputOfMultiJVMTests.overall).sorted.reverse.head, outputOfTests.events ++ outputOfMultiJVMTests.events, outputOfTests.summaries ++ outputOfMultiJVMTests.summaries)
      }
   )

}

object Dependencies {
    val akkaVersion = "2.4.4"
    val generalDependencies = Seq(
      "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion,

      "com.typesafe.akka" %% "akka-cluster"     % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence" % akkaVersion,

      "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.29",

      "com.typesafe.akka" %% "akka-testkit"            % akkaVersion % "test",
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % "test",

      "org.iq80.leveldb" % "leveldb" % "0.7",
      "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",

      "org.mockito"        % "mockito-core"   % "1.9.5"     % "test",
      "org.scalatest"     %% "scalatest"      % "2.2.1"     % "test"
    )
  }
