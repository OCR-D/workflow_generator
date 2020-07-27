#!/bin/bash
# This script should be placed in the ${TAVERNA_INST_DIR}

# Determine directory of script. 
ACTUAL_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
ACTUAL_TIME=`date +%Y_%m_%d_%H%M`

# Check if argument for workspace is given
if [ -z "$1" ]; then
  echo Please provide a directory where to find workspaces
  echo and the path where to store the results.
  echo USAGE:
  echo   $0 /path/to/all/manuscripts /path/to/results
  exit 1
fi

WORKSPACE_DIRECTORY=$(echo $1 | sed 's:/*$::')


# Check if directory exists
if [ -z "$2" ]; then
  echo Please provide a directory where to store the results
  echo USAGE:
  echo   $0 /path/to/all/manuscripts /path/to/results
  exit 1
fi

RESULT_DIRECTORY=$(echo $2 | sed 's:/*$::')

cd $ACTUAL_DIR

# Create unique directory for results.
mkdir -p "$RESULT_DIRECTORY/eval_$ACTUAL_TIME"
RESULT_DIRECTORY="$RESULT_DIRECTORY/eval_$ACTUAL_TIME"
echo Write results to $RESULT_DIRECTORY

for file in $WORKSPACE_DIRECTORY/*
do
        # Prepare manuscript
	echo $file
	filename=$(basename $file)
	echo $filename
	cp -r $file  workspace/

        # Start workflow
        bash startWorkflow.sh parameters_evaluation.txt workspace/$filename/data

        # Move result to resultDir
        mv workspace/$filename $RESULT_DIRECTORY

        # Clean up workspace to reduce memory.
        # For debugging comment the following line
        bash cleanUpWorkspace.sh $RESULT_DIRECTORY/$filename/data

        # Clean up temp directory
        rm -rf /tmp/ocrd-bagit*
done


