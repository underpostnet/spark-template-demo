package net.underpost.spec

import net.underpost.SparkSessionTestWrapper
import com.github.mrpowers.spark.fast.tests.DataFrameComparer
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import net.underpost.runner.TestRunner
import org.scalatest.funspec.AnyFunSpec
import org.apache.spark.sql.functions._

class GpuTestSpec extends AnyFunSpec
  with DataFrameComparer
  with SparkSessionTestWrapper {

  override val spark: SparkSession = TestRunner.sparkSessionInstance.getOrElse(
    throw new IllegalStateException("SparkSession is not initialized by TestRunner.")
  )

  import spark.implicits._

  describe("SqlOnGpuExample") {

    it("should correctly filter data using SQL and produce expected output") {
      val df = Seq(1, 2, 3).toDF("value")
      df.createOrReplaceTempView("df")

      val actual = spark.sql("SELECT value FROM df WHERE value <> 1")

      val expected = spark.createDataFrame(
        spark.sparkContext.parallelize(Seq(Row(2), Row(3))),
        StructType(Seq(StructField("value", IntegerType, nullable = true)))
      )

      assertSmallDataFrameEquality(actual, expected, ignoreNullable = true)
    }

    it("should process large dataset on GPU with SQL filter") {
      val largeDF = spark.range(0, 100000000).toDF("id")
      largeDF.createOrReplaceTempView("large_df")

      val result = spark.sql("SELECT COUNT(*) FROM large_df WHERE id % 2 = 0")

      // EXPLAIN GPU PLAN
      result.queryExecution.executedPlan.foreach { planNode =>
        println("== EXECUTION PLAN NODE ==")
        println(planNode)
      }

      result.show(false)
    }
  }
}
