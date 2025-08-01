// -------------------------------------------------------------------
// FILE: project/plugins.sbt
// -------------------------------------------------------------------
// This plugin is used to create a single "fat" JAR containing all
// dependencies, which is standard for Spark applications.
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.2.0")

// Add Spark as a dependency for the build definition itself.
// This is necessary so that `build.sbt` can compile code that uses Spark APIs,
// specifically for the `testOptions` setup.
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.5"
