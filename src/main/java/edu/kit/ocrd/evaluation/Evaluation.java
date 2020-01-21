/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.ocrd.evaluation;

import java.util.ArrayList;
import java.util.List;

/**
 * Hold all values needed for evaluation.
 */
public class Evaluation {

  private static final String fieldSeparator = "|";
  private double cer;
  private double wer;
  private int noOfCharacters;
  private int noOfWords;
  private String outputGroup;
  private String index;
  private List<EvaluationProcessor> processorList;
  private long durationWorkflow;
  
  public Evaluation() {
    processorList = new ArrayList<>();
  }

  public Evaluation(double wer, double cer, int noOfCharacters, int noOfWords, String resultFile) {
    processorList = new ArrayList<>();
    this.cer = cer;
    this.wer = wer;
    this.noOfCharacters = noOfCharacters;
    this.noOfWords = noOfWords;
    this.outputGroup = resultFile;
  }

  /**
   * @return the cer
   */
  public double getCer() {
    return cer;
  }

  /**
   * @param cer the cer to set
   */
  public void setCer(double cer) {
    this.cer = cer;
  }

  /**
   * @return the wer
   */
  public double getWer() {
    return wer;
  }

  /**
   * @param wer the wer to set
   */
  public void setWer(double wer) {
    this.wer = wer;
  }

  /**
   * @return the noOfCharacters
   */
  public int getNoOfCharacters() {
    return noOfCharacters;
  }

  /**
   * @param noOfCharacters the noOfCharacters to set
   */
  public void setNoOfCharacters(int noOfCharacters) {
    this.noOfCharacters = noOfCharacters;
  }

  /**
   * @return the noOfWords
   */
  public int getNoOfWords() {
    return noOfWords;
  }

  /**
   * @param noOfWords the noOfWords to set
   */
  public void setNoOfWords(int noOfWords) {
    this.noOfWords = noOfWords;
  }

  /**
   * @return the processorList
   */
  public List<EvaluationProcessor> getProcessorList() {
    return processorList;
  }

  /**
   * @param processorList the processorList to set
   */
  public void setProcessorList(List<EvaluationProcessor> processorList) {
    this.processorList = processorList;
  }

  /**
   * @param processor the processorList to set
   */
  public void addProcessor(EvaluationProcessor processor) {
    this.processorList.add(processor);
  }

  public static List<String> toCsvHeader(String message) {
    List<String> allLines = new ArrayList<>();
    allLines.add("# " + message + "\n");
    StringBuilder csvHeader = new StringBuilder();
    csvHeader.append("OutputGroup");
    csvHeader.append(fieldSeparator);
    csvHeader.append("Index");
    csvHeader.append(fieldSeparator);
    csvHeader.append("Processor");
    csvHeader.append(fieldSeparator);
    csvHeader.append("Step No.");
    csvHeader.append(fieldSeparator);
    csvHeader.append("Parameter");
    csvHeader.append(fieldSeparator);
    csvHeader.append("Duration Workflow[s]");
    csvHeader.append(fieldSeparator);
    csvHeader.append("Duration[s]");
    csvHeader.append(fieldSeparator);
    csvHeader.append("CER");
    csvHeader.append(fieldSeparator);
    csvHeader.append("WER");
    csvHeader.append(fieldSeparator);
    csvHeader.append("No of Characters");
    csvHeader.append(fieldSeparator);
    csvHeader.append("No of Words");
    csvHeader.append("\n");

    allLines.add(csvHeader.toString());
    
    return allLines;
  }

  public List<String> toCsvStrings() {
    List<String> allLines = new ArrayList<>();
    for (EvaluationProcessor item : processorList) {
      StringBuilder line = new StringBuilder();
      line.append(getOutputGroup());
      line.append(fieldSeparator);
      line.append(getIndex());
      line.append(fieldSeparator);
      line.append(item.getProcessor());
      line.append(fieldSeparator);
      line.append(item.getStepNo());
      line.append(fieldSeparator);
      line.append(item.getParameter());
      line.append(fieldSeparator);
      line.append(getDurationWorkflow());
      line.append(fieldSeparator);
      line.append(item.getDuration());
      line.append(fieldSeparator);
      line.append(getCer());
      line.append(fieldSeparator);
      line.append(getWer());
      line.append(fieldSeparator);
      line.append(getNoOfCharacters());
      line.append(fieldSeparator);
      line.append(getNoOfWords());
      line.append("\n");

      allLines.add(line.toString());
    }
    return allLines;
  }

  /**
   * @return the outputGroup
   */
  public String getOutputGroup() {
    return outputGroup;
  }

  /**
   * @param outputGroup the outputGroup to set
   */
  public void setOutputGroup(String outputGroup) {
    this.outputGroup = null;
    if (outputGroup != null) {
      this.outputGroup = outputGroup.trim();
    }
  }

  /**
   * @return the index
   */
  public String getIndex() {
    return index;
  }

  /**
   * @param index the index to set
   */
  public void setIndex(String index) {
    this.index = index;
  }

  /**
   * @return the durationWorkflow
   */
  public long getDurationWorkflow() {
    return durationWorkflow;
  }

  /**
   * @param durationWorkflow the durationWorkflow to set
   */
  public void setDurationWorkflow(long durationWorkflow) {
    this.durationWorkflow = durationWorkflow;
  }
}
