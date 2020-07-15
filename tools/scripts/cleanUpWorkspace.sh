#!/bin/bash
# Remove all files from workspace which are not needed for evaluation

# Check if argument for workspace is given
if [ -z "$1" ]; then
  echo Please provide a directory where to find workspace.
  echo USAGE:
  echo   $0 /path/to/workspace 
  exit 1
fi

WORKSPACE_DIRECTORY=$(echo $1 | sed 's:/*$::')

echo Clean up $WORKSPACE_DIRECTORY

# Clear input
# Remove all files except 
# - mets.xml
# - json and html files produced by dinglehopper
# - metadata/ocrd_provenance.xml
# - metadata/std[err|out].txt
find $WORKSPACE_DIRECTORY -name "*.zip" -exec rm '{}' \;
find $WORKSPACE_DIRECTORY -name "*.tif" -exec rm '{}' \;
find $WORKSPACE_DIRECTORY -name "*.png" -exec rm '{}' \;
find $WORKSPACE_DIRECTORY -name "*.jpg" -exec rm '{}' \;
find $WORKSPACE_DIRECTORY -name "*_0???.xml" -exec rm '{}' \;
find $WORKSPACE_DIRECTORY -name "mets.xml*_????" -exec rm '{}' \;
find $WORKSPACE_DIRECTORY -name "provenance_*.xml" -exec rm '{}' \;
rmdir $WORKSPACE_DIRECTORY/*
