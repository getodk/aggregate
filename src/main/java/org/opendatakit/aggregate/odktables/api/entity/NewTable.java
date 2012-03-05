package org.opendatakit.aggregate.odktables.api.entity;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.opendatakit.aggregate.odktables.entity.Column;

@XmlRootElement
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NewTable {

  private String tableId;
  private List<Column> columns;

}