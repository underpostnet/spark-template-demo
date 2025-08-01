package net.underpost.runner

import org.apache.spark.sql.SparkSession
import org.scalatest.tools.Runner
import net.underpost.SparkSessionTestWrapper // Import the trait

object TestRunner {

  // A companion object to hold the SparkSession, making it accessible to test suites
  // that mix in SparkSessionTestWrapper.
  // This ensures a single SparkSession is used and managed by the TestRunner.
  var sparkSessionInstance: Option[SparkSession] = None

  def main(args: Array[String]): Unit = {
    // Define the organization string as a Scala variable.
    // This value will be replaced by SBT during project generation.
    // In a real project, you might get this from a configuration file or environment variable.
    val organizationName = "net.underpost"

    // Create the SparkSession for the entire test run.
    // This SparkSession will be reused by all test suites.
    val spark = SparkSession.builder
      .appName("Spark Test Runner")
      // .master("local[*]") // Use local mode for running tests within the runner
      .config(
        "spark.sql.shuffle.partitions",
        "200" // Increased from 1 for better performance in a cluster environment.
      )
      .getOrCreate()

    // Set the SparkSession instance for other components to use
    sparkSessionInstance = Some(spark)

    println("=============================================")
    println("===          RUNNING SPARK TESTS          ===")
    println("=============================================")

    // Explicitly specify the test suite to run using the '-s' flag.
    // The '-o' flag prints results to standard out.
    // This bypasses ScalaTest's discovery mechanism (`-R` flag), which can be
    // unreliable in complex classloader environments like a Spark driver.
    // By directly naming the test class, we ensure it's loaded and run.
    // The fully qualified name must match the test's package and class name.
    val testResult = Runner.run(
      Array(
        "-o",
        // "-s",
        // organizationName + ".spec.HelloWorldSpec",
        "-s",
        organizationName + ".spec.GpuTestSpec"
      )
    )

    println("=============================================")
    println("===        SPARK TESTS COMPLETED          ===")
    println("=============================================")

    spark.stop()
    sparkSessionInstance = None // Clear the instance

    // Exit with a non-zero status code if tests failed.
    if (!testResult) {
      System.exit(1)
    }
  }
}
