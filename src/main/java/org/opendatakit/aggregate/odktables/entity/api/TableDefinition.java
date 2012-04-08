package org.opendatakit.aggregate.odktables.entity.api;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.opendatakit.aggregate.odktables.entity.Column;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TableDefinition {

  @ElementList(inline = true)
  private List<Column> columns;
}
