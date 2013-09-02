package org.opendatakit.aggregate.constants.common;

import java.io.Serializable;

/**
 * Enum of how binary data should be handled. Used when configuring aggregate's 
 * external services connections. 
 *  
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public enum BinaryOption implements Serializable {
  PROVIDE_LINKS("Provide Links to Media"),
  EMBED_BINARY("Include the Media Binary in Data");
  
  private String descriptionOfOption;
  
  private BinaryOption() {
    // GWT
  }
  
  private BinaryOption(String desc) {
    descriptionOfOption = desc;
  }
  
  public String getDescriptionOfOption() {
    return descriptionOfOption;
  }
  
  public String toString() {
    return descriptionOfOption;
  }
}
