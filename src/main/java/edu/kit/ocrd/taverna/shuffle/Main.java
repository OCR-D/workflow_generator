/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.ocrd.taverna.shuffle;

import java.io.BufferedWriter;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author hartmann-v
 */
public class Main {

  static HashMap<String, Integer> counterForOutputGroups = new HashMap<>();

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    // Erzeuge eine workflow_configuration aus allen in einer workflow_configuration_allProcessors.txt aufgeführten
    // Prozessoren und Parameter -Kombinationen.
    // Beginne mit INPUTGROUP OCR-D-IMG 
    // Ende mit OUTPUTGROUP OCR-D-RESULT
    Path get = Paths.get("/home/hartmann-v/Projekte/OCR-D/workflow_generator/src/main/resources/workflow_configuration_shuffle.txt");
    Path eval = Paths.get("/home/hartmann-v/Projekte/OCR-D/workflow_generator/src/main/resources/OCR-D-EVAL_0003.json");
    Path test = Paths.get("/home/hartmann-v/Projekte/OCR-D/workflow_generator/src/main/resources/workflow_configuration_test.txt");
    Path all = Paths.get("/home/hartmann-v/Projekte/OCR-D/workflow_generator/src/main/resources/workflow_configuration_all.txt");

//    List<Processor> allProcessors = shuffleAllProcessors(get);
//    for (Processor item : allProcessors) {
//      System.out.print(item.toConfigurationString());
//    }
    parseJsonFile(eval);
    System.out.println(getIndexOfGroupId("keinIndex"));
    System.out.println(getIndexOfGroupId("index_3"));
    System.out.println(getIndexOfGroupId("index_0004"));
    System.out.println(getIndexOfGroupId("index_wei_02"));
    System.out.println(getIndexOfGroupId("_01"));
    List<Processor> allProcessors = createTestWorkflow(get);
    for (Processor item : allProcessors) {
      System.out.print(item.toConfigurationString());
    }
    writeConfigFile("File for testing all available processor/parameter combinations.", allProcessors, test);
  }

  /**
   * Create one workflow to test all available processors of given configuration
   * file.
   *
   * Configuration file contains all processor/parameter combination which
   * should be tested. See workflow_configuration.txt
   *
   * @param message Message at the top of the file.
   * @param processors List of all processors
   * @param configurationFile config file with all processors.
   */
  public static void writeConfigFile(String message, List<Processor> processors, Path configurationFile) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(configurationFile.toFile()))) {
      writer.write("#");
      writer.write(message);
      writer.write("\n#\n");
      for (Processor processor : processors) {
        writer.write(processor.toConfigurationString());
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
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
    JSONParser parser = new JSONParser();

    try (Reader reader = new FileReader(evaluationFile.toString())) {

      JSONObject jsonObject = (JSONObject) parser.parse(reader);

      double wordErrorRate = (Double) jsonObject.get("wer");
      double characterErrorRate = (Double) jsonObject.get("cer");
      eval = new Evaluation();
      eval.setCer(characterErrorRate);
      eval.setWer(wordErrorRate);

    } catch (IOException e) {
      e.printStackTrace();
    } catch (ParseException ex) {
      ex.printStackTrace();
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
    String returnValue = groupId;
    if (index != null) {
      returnValue = String.format("%s_%05d", groupId, index);
    }
    return returnValue;
  }

  public static String getGroupId(String groupId) {
    Integer index = counterForOutputGroups.get(groupId);
    return getGroupId(groupId, index);
  }

  public static String getNextGroupId(String groupId) {
    if (counterForOutputGroups.get(groupId) == null) {
      counterForOutputGroups.put(groupId, 0);
    }
    int counter = counterForOutputGroups.get(groupId) + 1;
    counterForOutputGroups.put(groupId, counter);
    return getGroupId(groupId, counter);
  }
}
