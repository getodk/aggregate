package org.opendatakit.aggregate.odktables.entity;

/**
 * This represents information about a file so that a phone running ODKTables
 * will be able to check to see if it has the most recent version of the file,
 * and if not will be able to download the file. It is meant to be mostly a
 * struct that is parsed into and recovered from JSON, and work in tandem with
 * {@link OdkTablesKeyValueStoreEntry}.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesFileManifestEntry {

  /**
   * This is the name of the file.
   */
  public String filename;

  /**
   * This is the md5hash of the file, which will be used for checking whether or
   * not the version of the file on the phone is current.
   */
  public String md5hash;

  /**
   * This is the url from which the current version of the file can be
   * downloaded.
   */
  public String downloadUrl;


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((filename == null) ? 0 : filename.hashCode());
    result = prime * result + ((md5hash == null) ? 0 : md5hash.hashCode());
    result = prime * result + ((downloadUrl == null) ? 0 : downloadUrl.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if ( obj == null ) {
      return false;
    }
    if ( obj == this ) {
      return true;
    }
    if (!(obj instanceof OdkTablesFileManifestEntry)) {
      return false;
    }
    OdkTablesFileManifestEntry other = (OdkTablesFileManifestEntry) obj;
    return (filename == null ? other.filename == null : filename.equals(other.filename))
        && (md5hash == null ? other.md5hash == null : md5hash.equals(other.md5hash))
        && (downloadUrl == null ? other.downloadUrl == null : downloadUrl.equals(other.downloadUrl));
  }

}
