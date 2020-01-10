/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.ocrd.taverna.shuffle;

/**
 * Hold all values needed for evaluation.
 */
public class Evaluation {
  private double cer;
  private double wer;
  private int noOfCharacters;
  private int noOfWords;
  private String processor;
  private String parameters;

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
   * @return the processor
   */
  public String getProcessor() {
    return processor;
  }

  /**
   * @param processor the processor to set
   */
  public void setProcessor(String processor) {
    this.processor = processor;
  }

  /**
   * @return the parameters
   */
  public String getParameters() {
    return parameters;
  }

  /**
   * @param parameters the parameters to set
   */
  public void setParameters(String parameters) {
    this.parameters = parameters;
  }
}
