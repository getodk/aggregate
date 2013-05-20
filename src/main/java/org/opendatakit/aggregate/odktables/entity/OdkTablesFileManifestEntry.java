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
}
