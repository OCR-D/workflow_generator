Please copy all scripts to ${TAVERNA_INST_DIR}

Do permutation and evaluation of all/chosen processors
------------------------------------------------------
0. Create folder for workflow configuration files mkdir permutations in ${TAVERNA_INST_DIR} (if not exists)
    > cd ${TAVERNA_INST_DIR}
    > mkdir permutations
1. Copy all created workflow configuration files (workflow_configuration_permutation_????.txt ) in the permutations folder
    > cp workflow_configuration*.txt ${TAVERNA_INST_DIR}/permutations
2. Start evaluation
    > bash runPermutations.sh /path/to/workspace /path/to/resultdir
3. Create evaluation file
    > java -jar ./build/libs/WorkflowTool-0.1.0.jar eval /path/to/workspace [evaluation_results.csv] 
4. Import evalutation file into Excel/localc
    Please use the following settings:
      Character Encoding: UTF-8
      Language: Englisch(USA)
      Start at line: 2

      separator char: '|'
      combine separators: true

      extended number detection: true
5. Insert created cart into first cart of 'Evaluate_workflow_permutation.xlsx'
    Refresh pivot tables in other carts


Evaluate one workflow on different manuscripts
------------------------------------------
0. Prepare folder holding all manuscripts
  (e.g. all manuscripts from GT repo)
    > mkdir -p /path/to/all/manuscripts
    > cd /path/to/all/manuscripts
    > wget -O listOfContainers.json https://ocr-d-repo.scc.kit.edu/api/v1/metastore/bagit
    > ocrdzips=$(cat listOfContainers.json | tr ",[]\"" "\n")
    > for addr in $ocrdzips; do   wget $addr;   filename=$(basename -- "$addr");   directory="${filename%.*}";    mkdir $directory;   cd $directory;   unzip ../$filename;   cd ..; done
    > rm *.zip listOfContainers.json
1. Configure evaluation workflow
    > cp /path/to/selected_worklow_configuration.txt ${TAVERNA_INST_DIR}/conf/workflow_configuration4evaluation.txt
2. Start evaluation
    > bash runEvaluation4multipleManuscripts.sh /path/to/all/mnauscripts /path/to/resultdir
3. Create evaluation file
    > java -jar ./build/libs/WorkflowTool-0.1.0.jar evalGT /path/to/resultdir/eval_'date_of_eval' [evaluation_results.csv] 
4. Import evalutation file into Excel/localc
    Please use the following settings:
      Character Encoding: UTF-8
      Language: Englisch(USA)
      Start at line: 2

      separator char: '|'
      combine separators: true

      extended number detection: true
5. Insert created cart into first cart of 'Evaluate_workflow_multipleManuscripts.xlsx'
    Refresh pivot tables in other carts
  