package org.opendatakit.aggregate.odktables.api.entity;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.opendatakit.aggregate.odktables.entity.Row;

@XmlRootElement
@Data
@EqualsAndHashCode(callSuper = true)
public class RowResource extends Row {
  
  private String selfUri;
  
}
