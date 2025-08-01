#!/bin/bash

# Function to replace variables in a file based on a properties file.
# This version is more robust and cross-platform by building and using a temporary
# sed script file, which avoids issues with command line length limits and
# differences in `sed -i` syntax.
# Usage: replace_variables <file_path> <properties_file>
replace_variables() {
  local file_path="$1"
  local properties_file="$2"

  if [[ ! -f "$file_path" ]]; then
    echo "Error: File not found at '$file_path'"
    return 1
  fi

  if [[ ! -f "$properties_file" ]]; then
    echo "Error: Properties file not found at '$properties_file'"
    return 1
  fi

  echo "Processing file: $file_path"
  echo "Using properties from: $properties_file"

  # Read properties into an associative array (Bash 4+ required)
  declare -A props
  while IFS='=' read -r key value; do
    # Remove leading/trailing whitespace from key and value
    key=$(echo "$key" | xargs)
    value=$(echo "$value" | xargs)
    # Store in associative array
    props["$key"]="$value"
  done <"$properties_file"

  # Create a temporary file to hold the sed script.
  local sed_script=$(mktemp)

  # Iterate through the properties and write a sed substitution command for each.
  # We use a custom delimiter `|` to avoid conflicts with slashes in file paths.
  for key in "${!props[@]}"; do
    local value="${props[$key]}"
    # Escape special characters in the value for sed
    local escaped_value=$(echo "$value" | sed -e 's/[\/&|]/\\&/g')
    # Write the substitution commands to the temporary script file
    echo "s|\\\$${key}\\$|${escaped_value}|g" >> "$sed_script"
    echo "s|\\\${\\\?$key}|${escaped_value}|g" >> "$sed_script"
  done

  # Use `sed -i` with the `-f` flag to apply the script from the temporary file.
  # We use `sed -i` without an extension to perform in-place replacement on Linux systems.
  # The `|| sed -i '' -f "$sed_script" "$file_path"` provides a fallback for macOS.
  sed -i -f "$sed_script" "$file_path" || sed -i '' -f "$sed_script" "$file_path"
  
  # Clean up the temporary sed script file
  rm "$sed_script"

  echo "Successfully replaced variables in '$file_path'"
}

# --- Main script execution ---

echo "Creating dummy default.properties..."
cat <<EOF >default.properties
name = spark-template
organization = net.underpost
scalaVersionUsed = 2.12.18
scalaVersionUsedMajor = 2.12
sparkVersion = 3.5.5
rapidsVersion = 25.04.0
description = An sbt template for Apache Spark applications with GPU support and ScalaTest.
version = 0.0.11
EOF
echo "default.properties created."
echo ""

echo "Running variable replacement on Dockerfile..."
replace_variables "Dockerfile" "default.properties"
echo ""

echo "Running variable replacement on build.sbt..."
replace_variables "build.sbt" "default.properties"
echo ""

echo "Running variable replacement on build.sh..."
replace_variables "build.sh" "default.properties"
echo ""

echo "Running variable replacement on manifests/sparkapplication/spark-application.yaml..."
replace_variables "manifests/sparkapplication/spark-application.yaml" "default.properties"
echo ""

echo "Running variable replacement on manifests/sparkapplication/spark-test-runner-gpu.yaml..."
replace_variables "manifests/sparkapplication/spark-test-runner-gpu.yaml" "default.properties"
echo ""

echo "Cleaning up dummy default.properties..."
rm default.properties
echo "Cleanup complete."
