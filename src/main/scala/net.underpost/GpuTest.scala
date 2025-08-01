package net.underpost

import org.apache.spark.sql.SparkSession

object GpuTest {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("GpuTestApp")
      .getOrCreate()
    val sc = spark.sparkContext

    import spark.implicits._

    val viewName = "df"
    val df = sc.parallelize(Seq(1, 2, 3)).toDF("value")
    df.createOrReplaceTempView(viewName)

    println("=============================================")
    println("===          RUNNING GPU TEST APP         ===")
    println("=============================================")

    // Execute a SQL query and explain the plan (to see GPU optimizations)
    spark.sql("SELECT value FROM " + viewName + " WHERE value <>1").explain()
    // Show the results of the SQL query
    spark.sql("SELECT value FROM " + viewName + " WHERE value <>1").show()

    println("=============================================")
    println("===        GPU TEST APP COMPLETED         ===")
    println("=============================================")

    spark.stop()
  }
}
