package org.opendatakit.aggregate.constants.common;

import java.io.Serializable;

public enum ChartType implements Serializable {
  PIE_CHART("Pie Chart", "p3"),
  BAR_GRAPH("Bar Graph", "bvg"),
  SCATTER_PLOT("Scatter Plot", "s"),
  MAP("Map", "");
  
  private String displayText;
  private String optionText;
  
  private ChartType(String display, String option) {
    displayText = display;
    optionText = option;
  }
  
  public String getDisplayText() {
    return displayText;
  }

  public String getOptionText() {
    return optionText;
  }
}
