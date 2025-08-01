package net.underpost

import org.apache.spark.sql.SparkSession

/** A trait to provide a standardized SparkSession for test suites. The
  * SparkSession instance is expected to be provided by the concrete class
  * mixing in this trait, typically the test runner, ensuring a single, shared
  * SparkSession across all tests.
  */
trait SparkSessionTestWrapper {
  // Define an abstract spark session that will be provided by the TestRunner
  // This ensures that all tests use the same SparkSession instance managed by the runner.
  def spark: SparkSession
}
