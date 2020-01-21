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
public class EvaluationProcessor {

  private String processor;
  private Long stepNo;
  private String parameter;
  /** Duration in seconds. */
  private Long duration;
  private String inputGroup;

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
    this.processor = null;
    if (processor != null) {
    this.processor = processor.trim();
    }
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
    this.parameter = null;
    if (parameter != null) {
    this.parameter = parameter.trim();
    }
  }


  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder("Processor: \n");
    buffer.append("Processor - ").append(getProcessor()).append("\n");
    buffer.append("Parameter - ").append(getParameter()).append("\n");
    buffer.append("Time - ").append(getDuration()).append("\n");
    return buffer.toString();
  }

  /**
   * @return the duration
   */
  public Long getDuration() {
    return duration;
  }

  /**
   * @param duration the duration to set
   */
  public void setDuration(Long duration) {
    this.duration = duration;
  }

  /**
   * @return the inputGroup
   */
  public String getInputGroup() {
    return inputGroup;
  }

  /**
   * @param inputGroup the inputGroup to set
   */
  public void setInputGroup(String inputGroup) {
    this.inputGroup = inputGroup;
  }

  /**
   * @return the stepNo
   */
  public Long getStepNo() {
    return stepNo;
  }

  /**
   * @param stepNo the stepNo to set
   */
  public void setStepNo(Long stepNo) {
    this.stepNo = stepNo;
  }
}
