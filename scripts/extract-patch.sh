#!/bin/bash

# Input file containing the lines with file paths and content
input_file="$1"
output_dir="."

# Use awk to parse the input file and extract file paths and contents
awk '
  # Match lines that start with "// File:" and capture the relative file path
  /^\/\/ File:/ {
    # Close the previous file (if any)
    if (out != "") {
      close(out)
    }

    # Extract the file path and remove the // File: prefix
    file = substr($0, 10)
    out = "'"$output_dir"'/" file
    next
  }

  # Write content to the output file
  {
    if (out != "") {
      print $0 >> out
    }
  }
' "$input_file"
