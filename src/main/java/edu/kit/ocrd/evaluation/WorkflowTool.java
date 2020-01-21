/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.ocrd.evaluation;

import edu.kit.ocrd.workspace.entity.ProvenanceMetadata;
import edu.kit.ocrd.workspace.provenance.ProvenanceUtil;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.fzk.tools.xml.JaxenUtil;
import org.jdom.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author hartmann-v
 */
public class WorkflowTool {

  static HashMap<String, Integer> counterForOutputGroups = new HashMap<>();

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    String[] argsCoded = {"eval", "src/main/resources/weigel", "/tmp/eval_neu.txt"};
//    String[] argsCoded = {"shuffle", "src/main/resources/workflow_configuration_shuffle.txt", "/tmp/test.txt", "100"};
    args = argsCoded;

    if (args.length == 0) {
      printUsage("Missing parameters!");
      System.exit(1);
    }
    Path pathForTemplate = null;
    Path pathForResult = null;
    int maxNoOfProcessorSteps = 9000;
    switch (args[0]) {
      case "test":
        switch (args.length) {
          case 4:
            maxNoOfProcessorSteps = Integer.parseInt(args[3]);
          case 3:
            pathForResult = Paths.get(args[2]);
          case 2:
            pathForTemplate = Paths.get(args[1]);
            break;
          default:
            printUsage("Wrong number of parameters!");
            System.exit(1);
        }
        createTestWorkflow(pathForTemplate, pathForResult, maxNoOfProcessorSteps);
        break;
      case "shuffle":
        switch (args.length) {
          case 4:
            maxNoOfProcessorSteps = Integer.parseInt(args[3]);
          case 3:
            pathForResult = Paths.get(args[2]);
          case 2:
            pathForTemplate = Paths.get(args[1]);
            break;
          default:
            printUsage("Wrong number of parameters!");
            System.exit(1);
        }
        shuffleAllProcessors(pathForTemplate, pathForResult, maxNoOfProcessorSteps);
        break;
      case "eval":
        switch (args.length) {
          case 3:
            pathForResult = Paths.get(args[2]);
          case 2:
            pathForTemplate = Paths.get(args[1]);
            break;
          default:
            printUsage("Wrong number of parameters!");
            System.exit(1);
        }
        evaluateWorkspace(pathForTemplate, pathForResult);
        break;
      default:
        printUsage("Wrong parameter!");
        System.exit(1);
    } // Erzeuge eine workflow_configuration aus allen in einer workflow_configuration_allProcessors.txt aufgeführten
    // Prozessoren und Parameter -Kombinationen.
    // Beginne mit INPUTGROUP OCR-D-IMG 
    // Ende mit OUTPUTGROUP OCR-D-RESULT
    //    Path get = Paths.get("/home/hartmann-v/Projekte/OCR-D/workflow_generator/src/main/resources/workflow_configuration_shuffle.txt");
    //    Path eval = Paths.get("/home/hartmann-v/Projekte/OCR-D/workflow_generator/src/main/resources/OCR-D-EVAL_0003.json");
    //    Path test = Paths.get("/home/hartmann-v/Projekte/OCR-D/workflow_generator/src/main/resources/workflow_configuration_test.txt");
    //    Path all = Paths.get("/home/hartmann-v/Projekte/OCR-D/workflow_generator/src/main/resources/workflow_configuration_all.txt");
    //    
    //    List<Processor> allProcessors = shuffleAllProcessors(get);
    //    writeConfigFile("File for testing all possible workflows.", allProcessors, all);
    ////    for (Processor item : allProcessors) {
    ////      System.out.print(item.toConfigurationString());
    ////    }
    //    parseJsonFile(eval);
    //    System.out.println(getIndexOfGroupId("keinIndex"));
    //    System.out.println(getIndexOfGroupId("index_3"));
    //    System.out.println(getIndexOfGroupId("index_0004"));
    //    System.out.println(getIndexOfGroupId("index_wei_02"));
    //    System.out.println(getIndexOfGroupId("_01"));
    //    List<Processor> allProcessorsOnce = createTestWorkflow(get);
  }

  public static void printUsage(String message) {
    System.out.println(message);
    System.out.println("Please try one of the following:");
    System.out.println("  test path/to/workflow_configuration_holding_all_possible_processors.txt [workflow_configuration_new.txt] ");
    System.out.println("  shuffle path/to/workflow_configuration_holding_all_possible_processors.txt [workflow_configuration_new.txt] ");
    System.out.println("  eval  path/to/workspace [evaluation_results.csv] ");
  }

  /**
   * Evaluate the workspace by evaluating json files generated by dinglehopper
   *
   * @param pathToWorkspace
   * @param pathToResult csv file containing all results. (or STDOUT if null)
   */
  public static void evaluateWorkspace(Path pathToWorkspace, Path pathToResult) {
    try {
      String targetFile = "STDOUT";
      if (pathToResult != null) {
        targetFile = pathToResult.toAbsolutePath().toString();
      }
      System.out.println("Evaluate workspace and write results to '" + targetFile + "'");
      List<Evaluation> evaluationResult = evaluateWorkspace(pathToWorkspace);
      writeEvaluationFile("File for evaluating all available processor/parameter combinations.", evaluationResult, pathToResult);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static void createTestWorkflow(Path pathToTemplate, Path pathToResult, int maxNoOfProcessorSteps) {
    String targetFile = "STDOUT";
    if (pathToResult != null) {
      targetFile = pathToResult.toAbsolutePath().toString();
    }
    System.out.println("Create test workflow and write it to '" + targetFile + "'");
    System.out.println("Maximum number of processor steps per file: " + maxNoOfProcessorSteps);
    List<Processor> createTestWorkflow = createTestWorkflow(pathToTemplate);
    writeConfigFile("File for testing all available processor/parameter combinations.", createTestWorkflow, pathToResult, maxNoOfProcessorSteps);
  }

  public static void shuffleAllProcessors(Path pathToTemplate, Path pathToResult, int maxNoOfProcessorSteps) {
    String targetFile = "STDOUT";
    if (pathToResult != null) {
      targetFile = pathToResult.toAbsolutePath().toString();
    }
    System.out.println("Create all workflows and write it to '" + targetFile + "'");
    System.out.println("Maximum number of processor steps per file: " + maxNoOfProcessorSteps);
    List<Processor> createCompleteWorkflow = shuffleAllProcessors(pathToTemplate);
    writeConfigFile("File for testing all possible workflows.", createCompleteWorkflow, pathToResult, maxNoOfProcessorSteps);

  }

  /**
   * Write configuration file containing all processors and parameters with
   * unique file groups. If file contains to much processors it will be splitted
   * by appending index e.g.: '_001'
   *
   * @param message Message at the top of the file.
   * @param processors List of all processors
   * @param configurationFile config file with all processors.
   * @param maxNoOfProcessorSteps the maximum number of processor steps per
   * configuration file.
   */
  public static void writeConfigFile(String message, List<Processor> processors, Path configurationFile, int maxNoOfProcessorSteps) {
    if (configurationFile != null) {
      List<Processor> actualWorkflow = new ArrayList<>();
      int fileIndex = 1;
      boolean newFile = true;
      int stepCounter = 0;
      BufferedWriter bufferedWriter = null;
      Path pathToFile = configurationFile;
      try {
        for (Processor processor : processors) {
          if (stepCounter >= maxNoOfProcessorSteps) {
            newFile = true;
          }
          if (newFile) {
            if (bufferedWriter != null) {
              bufferedWriter.flush();
            }
            if (maxNoOfProcessorSteps < processors.size()) {
              pathToFile = Paths.get(String.format("%s_%03d", configurationFile.toString(), fileIndex++));
            }
            bufferedWriter = new BufferedWriter(new FileWriter(pathToFile.toFile()));
            bufferedWriter.write("# ");
            bufferedWriter.write(message);
            bufferedWriter.write("\n#File no. " + fileIndex + "\n");
            newFile = false;
            stepCounter = 0;
            for (Processor oldProcessors : actualWorkflow) {
              bufferedWriter.write(oldProcessors.toConfigurationString());
              stepCounter++;
            }
          }

          bufferedWriter.write(processor.toConfigurationString());
          stepCounter++;
          boolean replaceProcessor = false;
          String inputFileGrp = processor.getInputFileGrp().get(0).split("_")[0];
          int actualWorkflowLength = actualWorkflow.size();
          for (int workflowIndex = 0; workflowIndex < actualWorkflowLength; workflowIndex++) {
            Processor preProcessor = actualWorkflow.get(workflowIndex);
            String inputFileGrpWorkflow = preProcessor.getInputFileGrp().get(0).split("_")[0];
            if (inputFileGrp.equals(inputFileGrpWorkflow)) {
              replaceProcessor = true;
              actualWorkflow.set(workflowIndex, processor);
              int nextIndex = ++workflowIndex;
              for (; workflowIndex < actualWorkflowLength; workflowIndex++) {
                actualWorkflow.remove(nextIndex);
              }
            }
          }
          if (!replaceProcessor) {
            actualWorkflow.add(processor);
          }
        }
      } catch (IOException ex) {
        try {
          bufferedWriter.close();
        } catch (IOException ex1) {
          ex1.printStackTrace();
        }
        ex.printStackTrace();
      }
    } else {
      System.out.println("#" + message + "\n#\n");
      for (Processor processor : processors) {
        System.out.print(processor.toConfigurationString());
      }
    }
  }

  /**
   * Write configuration file containing all processors and parameters with
   * unique file groups.
   *
   * @param message Message at the top of the file.
   * @param evaluations List of all processors
   * @param configurationFile config file with all processors.
   */
  public static void writeEvaluationFile(String message, List<Evaluation> evaluations, Path configurationFile) {
    List<String> headerLines = Evaluation.toCsvHeader(message);
    if (configurationFile != null) {
      try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(configurationFile.toFile()))) {
        for (String line : headerLines) {
          bufferedWriter.write(line);
        }
        for (Evaluation evaluation : evaluations) {
          List<String> toCsvStrings = evaluation.toCsvStrings();
          for (String line : toCsvStrings) {
            bufferedWriter.write(line);
          }
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    } else {
      for (String line : headerLines) {
        System.out.print(line);
      }
      for (Evaluation evaluation : evaluations) {
        List<String> toCsvStrings = evaluation.toCsvStrings();
        for (String line : toCsvStrings) {
          System.out.print(line);
        }
      }
    }
  }

  /**
   * Create one workflow to test all available processors of given configuration
   * file.
   *
   * Configuration file contains all processor/parameter combination which
   * should be tested. See workflow_configuration.txt
   *
   * @param pathToWorkspace config file with all processors.
   */
  public static List<Evaluation> evaluateWorkspace(Path pathToWorkspace) throws Exception {
    List<Evaluation> evaluationResult = new ArrayList<>();
    Map<String, EvaluationProcessor> outputGroup2Processor = new HashMap<>();
    int total = 0;
    ProvenanceUtil pu;
    // Find all provenance files
    try (Stream<Path> walk = Files.walk(pathToWorkspace)) {

      List<String> result = walk.map(x -> x.toString())
              .filter(f -> f.endsWith("ocrd_provenance.xml")).collect(Collectors.toList());
      for (String file : result) {
        System.out.println("Provenance: " + file);
        File provenanceFile = Paths.get(file).toFile();
        Document provenanceDocument = JaxenUtil.getDocument(provenanceFile);
        // Read all processors with input and outputGroups.
        List<ProvenanceMetadata> extractWorkflows = ProvenanceUtil.extractWorkflows(provenanceDocument, null, "not needed");
        int before = outputGroup2Processor.size();
        int found = extractWorkflows.size();
        System.out.println("Found Processors: " + found);
        System.out.println("Size before: " + before);
        total += found;
        int failed = 0;
        for (ProvenanceMetadata item : extractWorkflows) {
//      Evaluation eval = new Evaluation();
//      eval.set
          EvaluationProcessor processor = new EvaluationProcessor();
          processor.setDuration(item.getDurationProcessor());
          processor.setParameter(item.getParameterFile());
          processor.setProcessor(item.getProcessorLabel());
          // As all Strings starts with '[' first string will be empty.
          processor.setInputGroup(item.getInputFileGrps().split("[\\[, \\]]+")[1].trim());
          if (item.getOutputFileGrps().length() > 3) {
            outputGroup2Processor.put(item.getOutputFileGrps().split("[\\[, \\]]+")[1].trim(), processor);
          } else 
            failed++;
        }
        System.out.println("Size before/failed/after/diff: " + before + "/" + failed + "/" + outputGroup2Processor.size() + "/" + (before + found - failed - outputGroup2Processor.size()));
        System.out.println("Total: " + total);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    // Find all json-files
    try (Stream<Path> walk = Files.walk(pathToWorkspace)) {

      List<String> result = walk.map(x -> x.toString())
              .filter(f -> f.endsWith(".json")).collect(Collectors.toList());

      for (String jsonFile : result) {
        Evaluation addEvaluationResult = parseJsonFile(Paths.get(jsonFile));
        String resultFile = addEvaluationResult.getOutputGroup();
        while (outputGroup2Processor.get(resultFile) != null) {
          EvaluationProcessor evProc = outputGroup2Processor.get(resultFile);
          addEvaluationResult.addProcessor(evProc);
          resultFile = evProc.getInputGroup();
        }
        long noOfTotalSteps = addEvaluationResult.getProcessorList().size();
        long step = 0;
        long durationWorkflow = 0;
        for (EvaluationProcessor item : addEvaluationResult.getProcessorList()) {
          item.setStepNo(noOfTotalSteps - step++);
          durationWorkflow += item.getDuration();
        }
        addEvaluationResult.setDurationWorkflow(durationWorkflow);
        evaluationResult.add(addEvaluationResult);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    return evaluationResult;
  }

  /**
   * Create one workflow to test all available processors of given configuration
   * file.
   *
   * Configuration file contains all processor/parameter combination which
   * should be tested. See workflow_configuration.txt
   *
   * @param configurationFile config file with all processors.
   */
  public static List<Processor> createTestWorkflow(Path configurationFile) {
    List<Processor> parseWorkflowConfiguration = parseWorkflowConfiguration(configurationFile);
    addProcessorForInputFileGrpForTest("OCR-D-IMG", parseWorkflowConfiguration);
    return parseWorkflowConfiguration;
  }

  public static Evaluation parseJsonFile(Path evaluationFile) {
    Evaluation eval = null;
    String[] jsonFileSplit = evaluationFile.toString().split("_");
    String index = jsonFileSplit[jsonFileSplit.length - 1].split("\\.")[0];
    JSONParser parser = new JSONParser();

    try (Reader reader = new FileReader(evaluationFile.toString())) {

      JSONObject jsonObject = (JSONObject) parser.parse(reader);

      double wordErrorRate = (Double) jsonObject.get("wer");
      double characterErrorRate = (Double) jsonObject.get("cer");
      String gt = ((String) jsonObject.get("gt")).split("/")[0];
      String ocr = ((String) jsonObject.get("ocr")).split("/")[0];
      String evalGrp;
      if (gt.split("_").length > 1) {
        evalGrp = gt;
      } else {
        evalGrp = ocr;
      }
      eval = new Evaluation();
      eval.setCer(characterErrorRate);
      eval.setWer(wordErrorRate);
      eval.setOutputGroup(evalGrp.trim());
      eval.setIndex(index);

    } catch (IOException | ParseException e) {
      e.printStackTrace();
    }
    return eval;
  }

  /**
   * Shuffle all processors from given configuration file.
   *
   * Configuration file contains all processor/parameter combination which
   * should be tested. See workflow_configuration.txt
   *
   * @param configurationFile config file with all processors.
   */
  public static List<Processor> shuffleAllProcessors(Path configurationFile) {
    Map<String, List<Processor>> parseWorkflowConfiguration = createMapOfWorkflowConfiguration(configurationFile);
    List<Processor> allProcessors = shuffleProcessors(parseWorkflowConfiguration);

    return allProcessors;
  }

  public static Map<String, List<Processor>> createMapOfWorkflowConfiguration(Path filePath) {
    Map<String, List<Processor>> processors = new HashMap<>();
    List<Processor> parseWorkflowConfiguration = parseWorkflowConfiguration(filePath);
    for (Processor processor : parseWorkflowConfiguration) {
      String inputFileGrp = processor.getInputFileGrp().get(0);
      List<Processor> listOfProcessors = processors.get(inputFileGrp);
      if (listOfProcessors == null) {
        listOfProcessors = new ArrayList<>();
        processors.put(inputFileGrp, listOfProcessors);
      }
      listOfProcessors.add(processor);
    }
    return processors;
  }

  public static List<Processor> parseWorkflowConfiguration(Path filePath) {
    List<Processor> allProcessors = new ArrayList<>();
    if (filePath.toFile().exists()) {
      List<String> lines;
      Processor processor = null;
      try {
        lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        for (String line : lines) {
          processor = Processor.parseOneLineOfWorkflowConfiguration(line);
          if (processor != null) {
            allProcessors.add(processor);
          }
        }

      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return allProcessors;
  }

  public static List<Processor> shuffleProcessors(Map<String, List<Processor>> allTemplates) {
    List<Processor> processors = new ArrayList<>();
    processors = addProcessorForInputFileGrp("OCR-D-IMG", null, allTemplates);
    return processors;
  }

  public static List<Processor> addProcessorForInputFileGrp(String inputFileGroup, Integer index, Map<String, List<Processor>> allTemplates) {
    List<Processor> allProcessors = new ArrayList<>();
    List<Processor> listOfNextProcessors = allTemplates.get(inputFileGroup);
    if (listOfNextProcessors != null) {
      // Select next possible processors
      for (Processor processor : listOfNextProcessors) {
        Processor newProcessor;
        newProcessor = Processor.parseOneLineOfWorkflowConfiguration(processor.toConfigurationString());
        List<String> fileGroupList = newProcessor.getInputFileGrp();
        for (int ifgIndex = 0; ifgIndex < fileGroupList.size(); ifgIndex++) {
          String inputFileGrp = fileGroupList.get(ifgIndex);
          fileGroupList.set(ifgIndex, getGroupId(inputFileGrp));
        }
        // for output file group(s) determine next index
        fileGroupList = newProcessor.getOutputFileGrp();
        for (int ofgIndex = 0; ofgIndex < fileGroupList.size(); ofgIndex++) {
          String outputFileGrp = fileGroupList.get(ofgIndex);
          fileGroupList.set(ofgIndex, getNextGroupId(outputFileGrp));
        }
        allProcessors.add(newProcessor);
        String firstOutputFileGrp = processor.getOutputFileGrp().get(0);
        List<Processor> processorsPart = addProcessorForInputFileGrp(firstOutputFileGrp, counterForOutputGroups.get(firstOutputFileGrp), allTemplates);
        allProcessors.addAll(processorsPart);
      }
    }
    return allProcessors;
  }

  public static void addProcessorForInputFileGrpForTest(String inputFileGroup, List<Processor> allProcessors) {
    for (Processor processor : allProcessors) {
      if (processor.getInputFileGrp().get(0).equals(inputFileGroup)) {
        List<String> fileGroupList = processor.getInputFileGrp();
        if (getIndexOfGroupId(fileGroupList.get(0)) == null) {
          for (int ifgIndex = 0; ifgIndex < fileGroupList.size(); ifgIndex++) {
            String inputFileGrp = fileGroupList.get(ifgIndex);
            if (counterForOutputGroups.get(inputFileGrp) != null) {
              fileGroupList.set(ifgIndex, getGroupId(inputFileGrp, 1));
            }
          }
          // for output file group(s) determine next index
          fileGroupList = processor.getOutputFileGrp();
          String nextFileGroup = fileGroupList.get(0);
          for (int ofgIndex = 0; ofgIndex < fileGroupList.size(); ofgIndex++) {
            String outputFileGrp = fileGroupList.get(ofgIndex);
            fileGroupList.set(ofgIndex, getNextGroupId(outputFileGrp));
          }
          addProcessorForInputFileGrpForTest(nextFileGroup, allProcessors);
        }
      }
    }
    return;
  }

  public static Integer getIndexOfGroupId(String fileGroup) {
    Integer returnValue = null;
    int lastIndexOf = fileGroup.lastIndexOf("_");
    if (lastIndexOf >= 0) {
      returnValue = Integer.parseInt(fileGroup.substring(++lastIndexOf));
    }
    return returnValue;
  }

  public static String getGroupId(String groupId, Integer index) {
    String returnValue = groupId.trim();
    if (index != null) {
      returnValue = String.format("%s_%05d", returnValue, index);
    }
    return returnValue;
  }

  public static String getGroupId(String groupId) {
    groupId = groupId.trim();
    Integer index = counterForOutputGroups.get(groupId);
    return getGroupId(groupId, index);
  }

  public static String getNextGroupId(String groupId) {
    groupId = groupId.trim();
    if (counterForOutputGroups.get(groupId) == null) {
      counterForOutputGroups.put(groupId, 0);
    }
    int counter = counterForOutputGroups.get(groupId) + 1;
    counterForOutputGroups.put(groupId, counter);
    return getGroupId(groupId, counter);
  }
}
