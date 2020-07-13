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
public class Evaluation4Gt {

  private static final String fieldSeparator = "|";
  private String manuscript;
  private String jsonFile;
  private double cer;
  private double wer;
  private int noOfCharacters;
  private int noOfWords;
  
  public Evaluation4Gt() {
  }

  public Evaluation4Gt(double wer, double cer, int noOfCharacters, int noOfWords, String manuscript, String jsonFile) {
    this.cer = cer;
    this.wer = wer;
    this.noOfCharacters = noOfCharacters;
    this.noOfWords = noOfWords;
    this.manuscript = manuscript;
    this.jsonFile = jsonFile;
  }

  public Evaluation4Gt(Evaluation eval, String manuscript, String jsonFile) {
    this.cer = eval.getCer();
    this.wer = eval.getWer();
    this.noOfCharacters = eval.getNoOfCharacters();
    this.noOfWords = eval.getNoOfWords();
    this.manuscript = manuscript;
    this.jsonFile = jsonFile;
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

  public static List<String> toCsvHeader(String message) {
    List<String> allLines = new ArrayList<>();
    allLines.add("# " + message + "\n");
    StringBuilder csvHeader = new StringBuilder();
    csvHeader.append("Manuscript");
    csvHeader.append(fieldSeparator);
    csvHeader.append("JsonFile");
    csvHeader.append(fieldSeparator);
    csvHeader.append("CER");
    csvHeader.append(fieldSeparator);
    csvHeader.append("WER");
    csvHeader.append(fieldSeparator);
    csvHeader.append("No of Characters");
    csvHeader.append(fieldSeparator);
    csvHeader.append("No of Words");
    csvHeader.append(fieldSeparator);
    csvHeader.append("No of Errors");
    csvHeader.append("\n");

    allLines.add(csvHeader.toString());
    
    return allLines;
  }

  public List<String> toCsvStrings() {
    List<String> allLines = new ArrayList<>();
      StringBuilder line = new StringBuilder();
      line.append(getManuscript());
      line.append(fieldSeparator);
      line.append(getJsonFile());
      line.append(fieldSeparator);
      line.append(getCer());
      line.append(fieldSeparator);
      line.append(getWer());
      line.append(fieldSeparator);
      line.append(getNoOfCharacters());
      line.append(fieldSeparator);
      line.append(getNoOfWords());
      line.append(fieldSeparator);
      line.append((int)(getNoOfCharacters() * getCer()));
      line.append("\n");

      allLines.add(line.toString());
    return allLines;
  }
  /**
   * @return the manuscript
   */
  public String getManuscript() {
    return manuscript;
  }

  /**
   * @param manuscript the manuscript to set
   */
  public void setManuscript(String manuscript) {
    this.manuscript = manuscript;
  }

  /**
   * @return the jsonFile
   */
  public String getJsonFile() {
    return jsonFile;
  }

  /**
   * @param jsonFile the jsonFile to set
   */
  public void setJsonFile(String jsonFile) {
    this.jsonFile = jsonFile;
  }
}
