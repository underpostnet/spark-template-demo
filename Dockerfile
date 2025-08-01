# Stage 1: Build the Scala application using sbt
# We use an official sbt image which contains the necessary tools (sbt, jdk).
# The image tag should match the Scala version in build.sbt (2.12.18).
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-11.0.17_8_1.9.3_2.12.18 as builder

WORKDIR /app

# Copy only the necessary build files first to leverage Docker layer caching.
# This ensures that dependencies are only re-downloaded if build.sbt changes.
COPY build.sbt .
COPY project/build.properties ./project/
COPY project/plugins.sbt ./project/

# This command will trigger sbt to download dependencies.
RUN sbt update

# Now copy the source code
COPY src ./src

# Run tests as part of the build process to ensure code quality.
# RUN sbt test

# Build the project and create the "fat" JAR file.
RUN sbt assembly


# Stage 2: Create the final runtime image
# We use an official Apache Spark image as the base.
# The Spark version (3.5.5) and Scala version (2.12.18) MUST match the
# dependencies in build.sbt and the base image tag.
FROM apache/spark:3.5.5-scala2.12-java17-python3-r-ubuntu

# The base image's WORKDIR is /opt/spark/work-dir, which is suitable.
# We copy our application JAR into /opt/spark/jars, which is on the
# default classpath for Spark.
COPY --from=builder /app/target/scala-2.12/spark-template.jar /opt/spark/jars/

# Download and set up the getGpusResources.sh script.
# Place it in /opt/spark/bin, which is a standard location for Spark scripts.
# This also avoids potential conflicts or unexpected behavior if Spark tries
# to stage files into the /opt/spark/scripts directory.
WORKDIR /opt/spark/scripts
RUN wget -O getGpusResources.sh https://raw.githubusercontent.com/apache/spark/master/examples/src/main/scripts/getGpusResources.sh \
    && chmod u+x getGpusResources.sh


WORKDIR /opt/spark/work-dir
