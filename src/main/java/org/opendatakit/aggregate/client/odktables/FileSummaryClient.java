package org.opendatakit.aggregate.client.odktables;

import org.opendatakit.aggregate.client.form.MediaFileSummary;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents a summary of a file that has been uploaded to be associated with
 * a table. 
 * <p>
 * Modeled on {@link MediaFileSummary}
 * @author sudar.sam@gmail.com
 *
 */
public class FileSummaryClient implements IsSerializable {

  /**
   * 
   */
  private static final long serialVersionUID = -6081623884490673459L;
  
  private String filename;
  private String contentType;
  private Long contentLength;
  // the list of media files associated with this file. Only media files
  // should be in this list, and they themselves should not have media files.
  private int numMediaFiles;
  private String key;
  // need to add and ID and change mediaFiles to numMediaFiles.
  // also should probably add the table name and table id.
  private String id; 
  private String tableId;
  
  @SuppressWarnings("unused")
  private FileSummaryClient() {
    // necessary for gwt serialization
  }
  
  public FileSummaryClient(String filename, String contentType, 
      Long contentLength, String key, int numMediaFiles, String id, 
      String tableId) {
    this.filename = filename;
    this.contentType = contentType;
    this.contentLength = contentLength;
    this.key = key;
    this.numMediaFiles = numMediaFiles;
    this.id = id;
    this.tableId = tableId;
  }
  
  public String getFilename() {
    return filename;
  }
  
  public String getContentType() {
    return contentType;
  }
  
  public Long getContentLength() {
    return contentLength;
  }
  
  public int getNumMediaFiles() {
    return numMediaFiles;
  }
  
  public String getKey() {
    return key;
  }
  
  public String getId() {
    return id;
  }

  public String getTableId() {
    return tableId;
  }
}
