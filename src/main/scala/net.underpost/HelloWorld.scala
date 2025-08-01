package net.underpost

import org.apache.spark.sql.{DataFrame, SparkSession}

object HelloWorld {

  /** Creates a DataFrame with a single "Hello, World!" message. This logic is
    * extracted to be testable.
    * @param spark
    *   The SparkSession.
    * @return
    *   A DataFrame with one column "message" and one row "Hello, World!".
    */
  def createGreetingDF(spark: SparkSession): DataFrame = {
    import spark.implicits._
    Seq("Hello, World!").toDF("message")
  }

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .appName("HelloWorld")
      .getOrCreate()
    createGreetingDF(spark).show(false)
    spark.stop()
  }
}
