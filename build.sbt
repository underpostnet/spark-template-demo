// -------------------------------------------------------------------
// FILE: build.sbt
// -------------------------------------------------------------------

import org.apache.spark.sql.SparkSession

// Define project versions.
// The Scala version should match the one Spark was built with in your Docker image.
// The apache/spark:3.5.5 image uses Scala 2.12.
val scalaVersionUsed = "$scalaVersionUsed$" // A recent version of Scala 2.12
val sparkVersion = "$sparkVersion$"

lazy val root = (project in file("."))
  .settings(
    name := "$name$",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scalaVersionUsed,
    organization := "$organization$",

    // Define resolvers (repositories) for dependencies. Maven Central is standard.
    resolvers += "Maven Central" at "https://repo1.maven.org/maven2/",

    // Add Spark dependencies. They are marked as "provided" because
    // the Spark runtime environment (in the Docker container) will
    // already have these libraries. This keeps our application JAR small.
    // The `%%` will correctly append the `_2.12` suffix.
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",

      // Test dependency for writing Spark tests. It is NOT scoped to "test"
      // so it will be included in the assembly JAR. This is required to allow
      // TestRunner to execute the tests on the Spark cluster.
      // spark-fast-tests brings in scalatest as a transitive dependency.
      "com.github.mrpowers" %% "spark-fast-tests" % "3.0.1",
      "org.scalatest" %% "scalatest" % "3.2.10",

      // Add RAPIDS Accelerator for Apache Spark core plugin.
      // IMPORTANT: Ensure this version is compatible with your Spark version.
      // "com.nvidia" is the correct group ID for the core RAPIDS artifacts.
      "com.nvidia" %% "rapids-4-spark" % "$rapidsVersion$"
    ),

    // sbt-assembly settings to create a runnable JAR
    assembly / mainClass := Some("$organization$.runner.TestRunner"),
    // Use a static JAR name to make the build process more robust.
    assembly / assemblyJarName := "$name$.jar",
    // Include test classes in the assembly JAR to allow running tests on the cluster.
    // assembly / test := true,
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "services", xs @ _*) =>
        MergeStrategy.concat // Concatenate service files
      case PathList("META-INF", xs @ _*) =>
        MergeStrategy.discard // Discard other META-INF
      case x => MergeStrategy.first
    },

    // Configure setup and cleanup actions for the 'sbt test' command.
    // This ensures a SparkSession is available for local tests run via sbt,
    // which is necessary for the Docker build step. We use the test classloader
    // (`loader`) to access the project's own compiled classes (like TestRunner)
    // via reflection. This is required because the build definition (build.sbt)
    // is compiled before the project's code and does not have direct access to it.
    Test / testOptions ++= Seq(
      Tests.Setup { loader =>
        // Create a local SparkSession before any tests run.
        val spark = SparkSession.builder
          .master("local[*]")
          .appName("sbt-test-session")
          .config("spark.driver.host", "localhost") // Often needed for local testing
          .getOrCreate()
        // Use the test classloader to get the TestRunner companion object and set the session.
        val testRunnerClass = loader.loadClass("net.underpost.runner.TestRunner$")
        val testRunnerModule = testRunnerClass.getField("MODULE$").get(null)
        val setter = testRunnerModule.getClass.getMethod("sparkSessionInstance_$eq", classOf[Option[SparkSession]])
        setter.invoke(testRunnerModule, Some(spark))
      },
      Tests.Cleanup { loader =>
        // Use the test classloader to get the TestRunner and clean up the session.
        val testRunnerClass = loader.loadClass("net.underpost.runner.TestRunner$")
        val testRunnerModule = testRunnerClass.getField("MODULE$").get(null)
        val getter = testRunnerModule.getClass.getMethod("sparkSessionInstance")
        val sessionOpt = getter.invoke(testRunnerModule).asInstanceOf[Option[SparkSession]]
        sessionOpt.foreach(_.stop())
        val setter = testRunnerModule.getClass.getMethod("sparkSessionInstance_$eq", classOf[Option[SparkSession]])
        setter.invoke(testRunnerModule, None)
      }
  )
)