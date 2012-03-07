package org.opendatakit.aggregate.odktables.entity.api;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.opendatakit.aggregate.odktables.entity.Column;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("tableDefinition")
@AllArgsConstructor
@Data
public class TableDefinition {
  
  @XStreamImplicit(itemFieldName="column")
  private List<Column> columns;
}
