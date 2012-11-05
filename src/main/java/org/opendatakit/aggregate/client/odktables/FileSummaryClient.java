package org.opendatakit.aggregate.client.odktables;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a summary of a file that has been uploaded to be associated with
 * a table. 
 * <p>
 * Modeled on {@link MediaFileSummary}
 * @author sudar.sam@gmail.com
 *
 */
public class FileSummaryClient implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -6081623884490673459L;
  
  private String filename;
  private String contentType;
  private Long contentLength;
  // the list of media files associated with this file. Only media files
  // should be in this list, and they themselves should not have media files.
  private List<FileSummaryClient> mediaFiles;
  private String key;
  
  public FileSummaryClient() {
  }
  
  public FileSummaryClient(String filename, String contentType, 
      Long contentLength, String key, List<FileSummaryClient> mediaFiles) {
    this.filename = filename;
    this.contentType = contentType;
    this.contentLength = contentLength;
    this.key = key;
    this.mediaFiles = mediaFiles;
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
  
  public List<FileSummaryClient> getMediaFiles() {
    return mediaFiles;
  }
  
  public String getKey() {
    return key;
  }

}
