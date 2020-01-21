/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.ocrd.evaluation;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hartmann-v
 */
public class Processor {

  private List<String> inputFileGrp;
  private List<String> outputFileGrp;
  private List<String> groupId;
  private String processor;
  private String parameter;
  private String errorLevel;
  /**
   * Parse String and create List of CSV values.
   *
   * @param commaSeparatedString String with items separated by ','.
   * @return List with all Strings.
   */
  public List<String> parseFileGrp(String commaSeparatedString) {
    List<String> list = new ArrayList<>();
    String[] split = commaSeparatedString.split(",");
    for (String item : split) {
      list.add(item.trim());
    }
    return list;
  }

  public void parseInputFileGrp(String commaSeparatedString) {
    setInputFileGrp(parseFileGrp(commaSeparatedString));
  }

  public void parseOutputFileGrp(String commaSeparatedString) {
    setOutputFileGrp(parseFileGrp(commaSeparatedString));
  }

  public void parseGroupId(String commaSeparatedString) {
    setGroupId(parseFileGrp(commaSeparatedString));
  }

  /**
   * @return the inputFileGrp
   */
  public List<String> getInputFileGrp() {
    return inputFileGrp;
  }

  /**
   * @param inputFileGrp the inputFileGrp to set
   */
  public void setInputFileGrp(List<String> inputFileGrp) {
    this.inputFileGrp = inputFileGrp;
  }

  /**
   * @return the outputFileGrp
   */
  public List<String> getOutputFileGrp() {
    return outputFileGrp;
  }

  /**
   * @param outputFileGrp the outputFileGrp to set
   */
  public void setOutputFileGrp(List<String> outputFileGrp) {
    this.outputFileGrp = outputFileGrp;
  }

  /**
   * @return the groupId
   */
  public List<String> getGroupId() {
    return groupId;
  }

  /**
   * @param groupId the groupId to set
   */
  public void setGroupId(List<String> groupId) {
    this.groupId = groupId;
  }

  /**
   * @return the processor
   */
  public String getProcessor() {
    return processor;
  }

  /**
   * @param processor the processor to set
   */
  public void setProcessor(String processor) {
    this.processor = processor.trim();
  }

  /**
   * @return the parameter
   */
  public String getParameter() {
    return parameter;
  }

  /**
   * @param parameter the parameter to set
   */
  public void setParameter(String parameter) {
    this.parameter = parameter.trim();
  }

  /**
   * @return the errorLevel
   */
  public String getErrorLevel() {
    return errorLevel;
  }

  /**
   * @param errorLevel the errorLevel to set
   */
  public void setErrorLevel(String errorLevel) {
    this.errorLevel = errorLevel.trim().toUpperCase();
  }

  /**
   * Parse one line of 'workflow_configuration.txt'
   *
   * @param line lne with parameters
   * @return NULL if not a valid line
   */
  public static Processor parseOneLineOfWorkflowConfiguration(String line) {
    Processor returnValue = null;
    if (!line.startsWith("#")) {
      String[] split = line.split("\\|");
      if (split.length == 6) {
        returnValue = new Processor();
        returnValue.setProcessor(split[0]);
        returnValue.parseInputFileGrp(split[1]);
        returnValue.parseOutputFileGrp(split[2]);
        returnValue.parseGroupId(split[3]);
        returnValue.setParameter(split[4]);
        returnValue.setErrorLevel(split[5]);
      } else {
        System.out.println("Ignore line: '" + line + "'");
      }
    }
    return returnValue;
  }

  public String toConfigurationString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append(getProcessor()).append(" | ");
    String comma = "";
    for (String item : inputFileGrp) {
      buffer.append(comma);
      comma = ",";
      buffer.append(item.trim());
    }
    buffer.append(" | ");
    comma = "";
    for (String item : outputFileGrp) {
      buffer.append(comma);
      comma = ",";
      buffer.append(item.trim());
    }
    buffer.append(" | ");
    comma = "";
    for (String item : groupId) {
      buffer.append(comma);
      comma = ",";
      buffer.append(item.trim());
    }
    buffer.append(" | ");
    buffer.append(getParameter()).append(" | ");
    buffer.append(getErrorLevel()).append("\n");
    return buffer.toString();
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder("Processor: \n");
    buffer.append("Processor - ").append(getProcessor()).append("\n");
    buffer.append("InputFileGrp - ").append("\n");
    for (String item : inputFileGrp) {
      buffer.append(" - ").append(item).append("\n");
    }
    buffer.append("OutputFileGrp - ").append("\n");
    for (String item : outputFileGrp) {
      buffer.append(" - ").append(item).append("\n");
    }
    buffer.append("GroupId - ").append("\n");
    for (String item : groupId) {
      buffer.append(" - ").append(item).append("\n");
    }
    buffer.append("Parameter - ").append(getParameter()).append("\n");
    buffer.append("ErrorLevel - ").append(getErrorLevel()).append("\n");
    return buffer.toString();
  }
}
