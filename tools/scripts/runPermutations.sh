#!/bin/bash
# This script should be placed in the ${TAVERNA_INST_DIR}

# Determine directory of script. 
ACTUAL_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
ACTUAL_TIME=`date +%Y_%m_%d_%H%M`

# Check if argument for workspace is given
if [ -z "$1" ]; then
  echo Please provide a directory where to find workspace
  echo and the path where to store the results.
  echo USAGE:
  echo   $0 /path/to/workspace /path/to/results
  exit 1
fi

WORKSPACE_DIRECTORY=$(echo $1 | sed 's:/*$::')


# Check if directory exists
if [ -z "$2" ]; then
  echo Please provide a directory where to store the results
  echo USAGE:
  echo   $0 /path/to/workspace /path/to/results
  exit 1
fi

RESULT_DIRECTORY=$(echo $2 | sed 's:/*$::')

cd $ACTUAL_DIR

# Create unique directory for results.
mkdir -p "$RESULT_DIRECTORY/$ACTUAL_TIME"

# Backup workspace
cp -r $WORKSPACE_DIRECTORY "$RESULT_DIRECTORY/$ACTUAL_TIME/orig"

mkdir -p permutations/done_$ACTUAL_TIME
for file in permutations/workflow_configuration_permutation*
do
        # Prepare config files
	echo $file
        cp $file conf/workflow_configuration4permutation.txt
        mv $file permutations/done_$ACTUAL_TIME 
        # Start workflow
        bash startWorkflow.sh parameters_permutation.txt $WORKSPACE_DIRECTORY
        # Move result to resultDir
        END_TIME=`date +%Y_%m_%d_%H%M%S`
        mv $WORKSPACE_DIRECTORY "$RESULT_DIRECTORY/$ACTUAL_TIME/eval_$END_TIME"
        # Clean up workspace to reduce memory.
        bash cleanUpWorkspace.sh "$RESULT_DIRECTORY/$ACTUAL_TIME/eval_$END_TIME"
        cp -r "$RESULT_DIRECTORY/$ACTUAL_TIME/orig" $WORKSPACE_DIRECTORY
        # Clean up temp directory
        rm -rf /tmp/ocrd-bagit*
done


