## Spark on Kubernetes Template

<img alt='apache_spark' src='https://img.shields.io/badge/apache_spark%20v3.5.5-100000?style=flat&logo=apache-spark&logoColor=FFFFFF&labelColor=C76E00&color=727273'/> <img alt='scala' src='https://img.shields.io/badge/scala%20v2.12-100000?style=flat&logo=scala&logoColor=FFFFFF&labelColor=8b0000&color=727273'>

[![Spark CI](https://github.com/underpostnet/spark-template-demo/actions/workflows/docker-image.yml/badge.svg)](https://github.com/underpostnet/spark-template-demo/actions/workflows/docker-image.yml)

This project provides template for building, testing, and deploying Scala Spark applications on Kubernetes using the Spark Operator.

### Core Features

- **Scala & sbt**: Leverages `sbt` for a standard Scala project structure, dependency management, and build automation.
- **Containerized Workflows**: A multi-stage `Dockerfile` ensures a lean and optimized Docker image for your Spark application, based on official Apache Spark images.
- **Kubernetes-Native Deployment**: Designed for seamless deployment and management via the Spark on Kubernetes Operator, utilizing `SparkApplication` custom resources.
- **Integrated Testing**:
  - Supports unit and integration testing with `scalatest` and `spark-fast-tests`.
  - Includes a `TestRunner` that enables **in-cluster testing**, allowing your test suites to execute directly on the Spark cluster in an environment identical to production.
- **GPU Acceleration Ready**: Pre-configured with RAPIDS Accelerator for Apache Spark, enabling GPU-accelerated Spark SQL and DataFrame operations.
- **Pre-configured RBAC**: Includes necessary Kubernetes Role-Based Access Control (`spark-rbac.yaml`) to grant the Spark driver the permissions required to create and manage its executor pods.
- **Ephemeral Storage Configuration**: Demonstrates how to configure ephemeral storage requests and limits for Spark driver and executor pods.
- **ConfigMap Integration**: Utilizes Kubernetes ConfigMaps (`spark-driver-pod-config`, `spark-executor-pod-config`) to inject pod spec fragments, allowing for flexible and advanced pod configurations.

### How to Use This Template

This project is designed as a [Giter8 template](https://www.foundweekends.org/giter8/index.html), making it easy to generate new Spark projects with all the pre-configured settings.

#### 1. Install sbt

If you don't have sbt installed, follow the instructions on the [official sbt website](https://www.scala-sbt.org/download.html). Ensure you have sbt launcher version 0.13.13 or above.

#### 2. Create a New Project from the Template

Open your terminal and run the following command.

```bash
sbt new underpostnet/spark-template.g8
```
