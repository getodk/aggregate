package org.opendatakit.aggregate.client.odktables;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesKeyValueStoreEntryClient implements IsSerializable {
  
  public OdkTablesKeyValueStoreEntryClient() { 
    // necessary for gwt serialization
  }

  public String tableId;
  public String partition;
  public String aspect;
  public String key;
  public String type;
  public String value;
}
