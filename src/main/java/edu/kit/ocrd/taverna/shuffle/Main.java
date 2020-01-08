/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.ocrd.taverna.shuffle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    Path get = Paths.get("/home/hartmann-v/Projekte/OCR-D/TestWorkflows/src/main/resources/workflow_configuration_shuffle.txt");

    System.out.println(getGroupId("test", 5));
    Map<String, List<Processor>> parseWorkflowConfiguration = parseWorkflowConfiguration(get);
    List<Processor> allProcessors = shuffleProcessors(parseWorkflowConfiguration);
    for (Processor item : allProcessors){
      System.out.print(item.toConfigurationString());
    }
  }

  public static Map<String, List<Processor>> parseWorkflowConfiguration(Path filePath) {
    Map<String, List<Processor>> processors = new HashMap<>();
    List<String> lines;
        Processor processor = null;
    try {
      lines
              = Files.readAllLines(filePath, StandardCharsets.UTF_8);
      for (String line : lines) {
         processor = Processor.parseOneLineOfWorkflowConfiguration(line);
        if (processor != null) {
          System.out.println(processor.toConfigurationString());
          System.out.println(processor);
          String inputFileGrp = processor.getInputFileGrp().get(0);
           List<Processor> listOfProcessors = processors.get(inputFileGrp);
           if (listOfProcessors == null) {
             listOfProcessors = new ArrayList<>();
             processors.put(inputFileGrp, listOfProcessors);
           }
          listOfProcessors.add(processor);
        }
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return processors;
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
        for (int ifgIndex = 0; ifgIndex < newProcessor.getInputFileGrp().size(); ifgIndex++) {
          newProcessor.getInputFileGrp().set(ifgIndex, getGroupId(newProcessor.getInputFileGrp().get(ifgIndex)));
        }
        for (int ofgIndex = 0; ofgIndex < newProcessor.getOutputFileGrp().size(); ofgIndex++) {
          String outputFileGrp = newProcessor.getOutputFileGrp().get(ofgIndex);
          newProcessor.getOutputFileGrp().set(ofgIndex, getNextGroupId(outputFileGrp));
        }
          allProcessors.add(newProcessor);
          String firstOutputFileGrp = processor.getOutputFileGrp().get(0);
        List<Processor> processorsPart = addProcessorForInputFileGrp(firstOutputFileGrp, counterForOutputGroups.get(firstOutputFileGrp), allTemplates);
        allProcessors.addAll(processorsPart);
      }
    }
    return allProcessors;
  }

  public static String getGroupId(String groupId) {
    Integer index = counterForOutputGroups.get(groupId);
    return getGroupId(groupId, index);
  }
  public static String getGroupId(String groupId, Integer index) {
    String returnValue = groupId;
     if (index != null)
     returnValue = String.format("%s_%05d", groupId, index);
    return returnValue;
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
