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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.fzk.tools.xml.JaxenUtil;
import org.jdom.Document;
import org.jdom.Namespace;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Helper tool for evaluating processors by comparing OCR results with
 * dinglehopper.
 */
public class WorkflowTool {

  static HashMap<String, Integer> counterForOutputGroups = new HashMap<>();

  static HashMap<String, Integer> noOfCharactersPerPage = new HashMap<>();

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    args = new String[]{"evalGT", "/media/hartmann-v/0C421C250C421C25/OCR-D/eval", "/tmp/testEval/eval.csv"};
    if (args.length == 0) {
      printUsage("Missing parameters!");
      System.exit(1);
    }
    Path pathForTemplate = null;
    Path pathForResult = null;
    int maxNoOfProcessorSteps = 9000;
    switch (args[0]) {
      case "evalGT":
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
        evaluateWorkspace4Gt(pathForTemplate, pathForResult);
        break;
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
      case "permutate":
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
        WorkflowTool.permutateAllProcessors(pathForTemplate, pathForResult, maxNoOfProcessorSteps);
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
      case "listProv":
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
        evaluateWorkflows(pathForTemplate, pathForResult);
        break;
      default:
        printUsage("Wrong parameter!");
        System.exit(1);
    }
  }

  public static void printUsage(String message) {
    System.out.println(message);
    System.out.println("Usage:");
    System.out.println("  test      path/to/workflow_configuration4permuation.txt [workflow_configuration_new.txt] ");
    System.out.println("  permutate path/to/workflow_configuration4permuation.txt [workflow_configuration_new.txt [max_no_of_processor_steps_per_file]] ");
    System.out.println("  eval      path/to/workspace [evaluation_results.csv] ");
    System.out.println("  evalGT    path/to/workspace [evaluation_results.csv] ");
    System.out.println("  listProv  path/to/workspace");
    System.out.println("\n\nExplanation:\n");
    System.out.println("Tool for creating workflow configuration file(s) used by taverna workflow.\n");
    System.out.println("test      - Compiles a workflow_configuration file with all active processors.");
    System.out.println("permutate - Compiles workflow_configuration file(s) with all active processors.");
    System.out.println("eval      - Evaluates the json files generated by dinglehopper and collect provenance information.");
    System.out.println("evalGT    - Evaluates the json files generated by dinglehopper.");
    System.out.println("listProv  - Evaluates the provenance file(s) and prints the input/output file groups of each processor.");
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

  /**
   * Evaluate the workspace by evaluating json files generated by dinglehopper
   *
   * @param pathToWorkspace
   * @param pathToResult csv file containing all results. (or STDOUT if null)
   */
  public static void evaluateWorkspace4Gt(Path pathToWorkspace, Path pathToResult) {
    try {
      String targetFile = "STDOUT";
      if (pathToResult != null) {
        targetFile = pathToResult.toAbsolutePath().toString();
      }
      System.out.println("Evaluate workspace and write results to '" + targetFile + "'");
      List<Evaluation4Gt> evaluationResult = evaluateWorkspace4Gt(pathToWorkspace);
      writeEvaluationFile4Gt("File for evaluating manuscripts.", evaluationResult, pathToResult);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Evaluate processors by parsing provenance file.
   *
   * Which processor produces which output file group.
   *
   * @param pathToWorkspace path to workspace.
   * @param pathToResult csv file containing all results. (or STDOUT if null)
   */
  public static void evaluateWorkflows(Path pathToWorkspace, Path pathToResult) {
    try {
      String targetFile = "STDOUT";
      if (pathToResult != null) {
        targetFile = pathToResult.toAbsolutePath().toString();
      }
      System.out.println("Evaluate processors and write their input and output file groups to '" + targetFile + "'");
      evaluateWorkflows(pathToWorkspace);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Evaluate the workspace by evaluating json files generated by dinglehopper
   *
   * @param pathToWorkspace
   */
  public static void determineNoOfCharacters(Path pathToWorkspace) {
    String targetFile = "STDOUT";
    System.out.println("Count no of characters per page and write results to '" + targetFile + "'");
    collectNoOfCharactersPerPage(pathToWorkspace, "OCR-D-GT-SEG-PAGE");
    collectNoOfCharactersPerPage(pathToWorkspace, "OCR-D-GT-SEG-BLOCK");
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

  public static void permutateAllProcessors(Path pathToTemplate, Path pathToResult, int maxNoOfProcessorSteps) {
    String targetFile = "STDOUT";
    if (pathToResult != null) {
      targetFile = pathToResult.toAbsolutePath().toString();
    }
    System.out.println("Permutate all processors and write them to '" + targetFile + "'");
    System.out.println("Maximum number of processor steps per file: " + maxNoOfProcessorSteps);
    List<Processor> createCompleteWorkflow = permutateAllProcessors(pathToTemplate);
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
              bufferedWriter.close();
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
        if (bufferedWriter != null) {
          bufferedWriter.flush();
          bufferedWriter.close();
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
   * Write configuration file containing all processors and parameters with
   * unique file groups.
   *
   * @param message Message at the top of the file.
   * @param evaluations List of all processors
   * @param configurationFile config file with all processors.
   */
  public static void writeEvaluationFile4Gt(String message, List<Evaluation4Gt> evaluations, Path configurationFile) {
    List<String> headerLines = Evaluation4Gt.toCsvHeader(message);
    if (configurationFile != null) {
      try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(configurationFile.toFile()))) {
        for (String line : headerLines) {
          bufferedWriter.write(line);
        }
        for (Evaluation4Gt evaluation : evaluations) {
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
      for (Evaluation4Gt evaluation : evaluations) {
        List<String> toCsvStrings = evaluation.toCsvStrings();
        for (String line : toCsvStrings) {
          System.out.print(line);
        }
      }
    }
  }

  /**
   * Create a list of Evaluation results.
   *
   * Configuration file contains all processor/parameter combination which
   * should be tested. See workflow_configuration.txt
   *
   * @param pathToWorkspace config file with all processors.
   * @return a list with all evaluation results.
   */
  public static List<Evaluation> evaluateWorkspace(Path pathToWorkspace) throws Exception {
    List<Evaluation> evaluationResult = new ArrayList<>();
    Map<String, EvaluationProcessor> outputGroup2Processor = new HashMap<>();
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
        for (ProvenanceMetadata item : extractWorkflows) {
          EvaluationProcessor processor = new EvaluationProcessor();
          processor.setDuration(item.getDurationProcessor());
          processor.setParameter(item.getParameterFile());
          processor.setProcessor(item.getProcessorLabel());
          // As all Strings starts with '[' first string will be empty.
          processor.setInputGroup(item.getInputFileGrps().split("[\\[, \\]]+")[1].trim());
          if (item.getOutputFileGrps().length() > 3) {
            String[] allOutputGroups = item.getOutputFileGrps().split("[\\[, \\]]+");
            for (String group : allOutputGroups) {
              outputGroup2Processor.put(group, processor);
            }
          }
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    // Calculate no of characters per page
    determineNoOfCharacters(pathToWorkspace);
    // Find all json-files
    try (Stream<Path> walk = Files.walk(pathToWorkspace)) {

      List<String> result = walk.map(x -> x.toString())
              .filter(f -> f.endsWith(".json")).collect(Collectors.toList());

      for (String jsonFile : result) {
        Evaluation addEvaluationResult = parseJsonFile(Paths.get(jsonFile));
        if (addEvaluationResult == null) {
          System.out.println("No evaluation possible for file: " + jsonFile);
        } else {
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
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    return evaluationResult;
  }

  /**
   * Create a list of Evaluation results.
   *
   * Configuration file contains all processor/parameter combination which
   * should be tested. See workflow_configuration.txt
   *
   * @param pathToWorkspace config file with all processors.
   * @return a list with all evaluation results.
   */
  public static List<Evaluation4Gt> evaluateWorkspace4Gt(Path pathToWorkspace) throws Exception {
    List<Evaluation4Gt> evaluationResult = new ArrayList<>();
    // Find all json-files
    try (Stream<Path> walk = Files.walk(pathToWorkspace)) {

      List<String> result = walk.map(x -> x.toString())
              .filter(f -> f.endsWith(".json")).collect(Collectors.toList());
       for (String jsonFile : result) {
        String[] split = jsonFile.split(File.separator);
        String manuscript = split[split.length - 4];
       Evaluation addEvaluationResult = parseJsonFile(Paths.get(jsonFile));
        if (addEvaluationResult == null) {
          System.out.println("No evaluation possible for file: " + jsonFile);
        } else {
       Evaluation4Gt eval = new Evaluation4Gt(addEvaluationResult, manuscript, jsonFile);
          evaluationResult.add(eval);
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    return evaluationResult;
  }

  /**
   * Evaluate all workflows.
   *
   * Print all processors and its input and output file groups.
   *
   * @param pathToWorkspace config file with all processors.
   * @throws java.lang.Exception
   */
  public static void evaluateWorkflows(Path pathToWorkspace) throws Exception {
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
        System.out.println("++++++++++++++++Workflows found: " + extractWorkflows.size());
        for (ProvenanceMetadata item : extractWorkflows) {
          System.out.println(item.getProcessorLabel() + " - " + item.getInputFileGrps() + " -> " + item.getOutputFileGrps());
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    return;
  }

  /**
   * Collect the number of characters for all pages and store them in local map.
   *
   * @param pathToWorkspace Path to (at least one) workspace (looking also in
   * subdirectories)
   * @param groupId GROUPID of ground truth.
   */
  private static void collectNoOfCharactersPerPage(Path pathToWorkspace, String groupId) {
    System.out.println("Path: '" + pathToWorkspace.toString() + "' looking for GROUPID '" + groupId + "'");
    Namespace page = Namespace.getNamespace("pc", "http://schema.primaresearch.org/PAGE/gts/pagecontent/2019-07-15");
    Namespace page2018 = Namespace.getNamespace("pc2018", "http://schema.primaresearch.org/PAGE/gts/pagecontent/2018-07-15");
    Namespace[] all = {page, page2018};
    // Find all provenance files
    try (Stream<Path> walk = Files.walk(pathToWorkspace)) {

      List<String> result = walk.map(x -> x.toString())
              .filter(f -> f.contains(groupId)).filter(f -> f.endsWith(".xml")).collect(Collectors.toList());
      for (String file : result) {
        File gtFile = Paths.get(file).toFile();
        String fileName = gtFile.getName();
        Document provenanceDocument = JaxenUtil.getDocument(gtFile);
        String[] values = JaxenUtil.getValues(provenanceDocument, "//pc:TextRegion/pc:TextEquiv/pc:Unicode", all);
        if (values.length == 0) {
          // try with old namespace
          values = JaxenUtil.getValues(provenanceDocument, "//pc2018:TextRegion/pc2018:TextEquiv/pc2018:Unicode", all);
        }
        int noOfCharacters = 0;
        for (String text : values) {
          noOfCharacters += text.length();
        }
        System.out.println("Total no of characters for file '" + fileName + "': " + noOfCharacters);
        noOfCharactersPerPage.put(fileName, noOfCharacters);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Create one workflow to test all available processors of given configuration
   * file.Configuration file contains all processor/parameter combination which
   * should be tested.
   *
   * See workflow_configuration.txt
   *
   * @param configurationFile config file with all processors.
   * @return List with all processors to be executed once.
   */
  public static List<Processor> createTestWorkflow(Path configurationFile) {
    List<Processor> parseWorkflowConfiguration = parseWorkflowConfiguration(configurationFile);
    addProcessorForInputFileGrpForTest("OCR-D-IMG", parseWorkflowConfiguration);
    return parseWorkflowConfiguration;
  }

  /**
   * Parse json file containing CER.
   *
   * @param evaluationFile Json file containing results from evaluation
   * @return Evaluation with parsed attributes.
   */
  public static Evaluation parseJsonFile(Path evaluationFile) {
    Evaluation eval = null;
    String[] jsonFileSplit = evaluationFile.toString().split("_");
    String index = jsonFileSplit[jsonFileSplit.length - 1].split("\\.")[0];
    JSONParser parser = new JSONParser();
    System.out.println(evaluationFile.toString());
    try (Reader reader = new FileReader(evaluationFile.toString())) {

      JSONObject jsonObject = (JSONObject) parser.parse(reader);

      double wordErrorRate = (Double) jsonObject.get("wer");
      double characterErrorRate = (Double) jsonObject.get("cer");
      String[] gt = ((String) jsonObject.get("gt")).split("/");
      String[] ocr = ((String) jsonObject.get("ocr")).split("/");
      String evalGrp = ocr[0];
      String gtFilename = gt[1];
//      if (gt[0].split("_").length > 1) {
      evalGrp = gt[0];
      gtFilename = ocr[1];
//      }
      eval = new Evaluation();
      eval.setCer(characterErrorRate);
      eval.setWer(wordErrorRate);
      eval.setOutputGroup(evalGrp.trim());
      eval.setIndex(index);
      System.out.println("---- " + gtFilename + " " + ocr[1]);
      try {
        long noOfCharacters = (Long) jsonObject.get("n_characters");
        eval.setNoOfCharacters((int) noOfCharacters);
      } catch (NullPointerException npe) {
      eval.setNoOfCharacters(noOfCharactersPerPage.get(gtFilename));
      }
      try {
        long noOfWords = (Long) jsonObject.get("n_words");
        eval.setNoOfWords((int) noOfWords);
      } catch (NullPointerException npe) {
        // try determine no of characters using GT afterwards
      }

    } catch (IOException | ParseException | ClassCastException e) {
      e.printStackTrace();
    }
    return eval;
  }

  /**
   * Shuffle all processors from given configuration file. Configuration file
   * contains all processor/parameter combination which should be tested.
   *
   * See workflow_configuration.txt
   *
   * @param configurationFile config file with all processors.
   * @return List with all possible processors in correct order.
   */
  public static List<Processor> permutateAllProcessors(Path configurationFile) {
    Map<String, List<Processor>> parseWorkflowConfiguration = createMapOfWorkflowConfiguration(configurationFile);
    List<Processor> allProcessors = permutateProcessors(parseWorkflowConfiguration);

    return allProcessors;
  }

  /**
   * Map all processors dependent on its input file group.
   *
   * @param filePath File containing all possible processors.
   * @return Map with all processors referenced by their input file group.
   */
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

  /**
   * Parse workflow configuration with all processors.
   *
   * @param filePath Path to workflow_configuration.txt
   * @return List with all processors defined in file.
   */
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
    } else {
      System.out.println("File '" + filePath.toString() + "' doesn't exist!");
    }
    System.out.println("Found " + allProcessors.size() + " processors.");
    return allProcessors;
  }

  /**
   * Permutate all processors.
   *
   * @param allTemplates Map with all given processors.
   * @return List with the processors in correct order.
   */
  public static List<Processor> permutateProcessors(Map<String, List<Processor>> allTemplates) {
    List<Processor> processors = new ArrayList<>();
    processors = addProcessorForInputFileGrp("OCR-D-IMG", allTemplates);
    return processors;
  }

  /**
   * Add all processors with given input file group to the list with all
   * processors. For the output file groups of each processor itself the method
   * is called recursive.
   *
   * @param inputFileGroup Input file group of processor
   * @param allTemplates Map with all processors mapped via input file group.
   * @return List with all processors in correct order.
   */
  public static List<Processor> addProcessorForInputFileGrp(String inputFileGroup, Map<String, List<Processor>> allTemplates) {
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
        List<Processor> processorsPart = addProcessorForInputFileGrp(firstOutputFileGrp, allTemplates);
        allProcessors.addAll(processorsPart);
      }
    }
    return allProcessors;
  }

  /**
   * Add unique output file group to given processors. Each processor should be
   * available only once.
   *
   * @param inputFileGroup Input file group of processor
   * @param allProcessors List with all processors.
   * @return List with all processors in correct order.
   */
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
  }

  /**
   * Parse current index for given file group
   *
   * @param fileGroup Label of the file group.
   * @return Current index of given file group.
   */
  public static Integer getIndexOfGroupId(String fileGroup) {
    Integer returnValue = null;
    int lastIndexOf = fileGroup.lastIndexOf("_");
    if (lastIndexOf >= 0) {
      returnValue = Integer.parseInt(fileGroup.substring(++lastIndexOf));
    }
    return returnValue;
  }

  /**
   * Get group ID. If no index is given index won't be appended.
   *
   * @param groupId group ID
   * @param index index
   * @return groupId with index
   */
  public static String getGroupId(String groupId, Integer index) {
    String returnValue = groupId.trim();
    if (index != null) {
      returnValue = String.format("%s_%05d", returnValue, index);
    }
    return returnValue;
  }

  /**
   * Get group ID with current index for given group ID
   *
   * @param groupId Group ID
   * @return Group ID with current index
   */
  public static String getGroupId(String groupId) {
    groupId = groupId.trim();
    Integer index = counterForOutputGroups.get(groupId);
    return getGroupId(groupId, index);
  }

  /**
   * Determine next index for given group ID.
   *
   * @param groupId Group ID
   * @return Group ID with new index.
   */
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
