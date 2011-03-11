package org.opendatakit.aggregate.server;

import java.util.List;

import org.opendatakit.aggregate.client.form.KmlSettings;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.Form;

public class GenerateKmlSettings {

  private KmlSettings settings;
  private Form form;
  
  public GenerateKmlSettings(Form form) {
    this.form = form;
    this.settings = new KmlSettings();
  }
  
  public KmlSettings generate() {
    FormElementModel root = form.getTopLevelGroupElement();
    processElementForColumnHead(form, root, root);
    return settings;
  }
  
  /**
   * Helper function to recursively go through the element tree and create the
   * FormElementKeys
   * 
   */
  private void processElementForColumnHead(Form form, FormElementModel node, FormElementModel root) {
    if (node == null)
      return;

    FormElementKey key = node.constructFormElementKey(form);
    String nodeName = key.userFriendlyString(form);
    switch (node.getElementType()) {
    case GEOPOINT:
      settings.addGeopointNode(nodeName, key.toString());
      break;
    case BINARY:
      settings.addBinaryNode(nodeName, key.toString());
      break;
    case REPEAT:
    case GROUP:
      break; // should not be in any list
    default:
      settings.addNode(nodeName, key.toString());
    }

    List<FormElementModel> childDataElements = node.getChildren();
    if (childDataElements == null) {
      return;
    }
    for (FormElementModel child : childDataElements) {
      processElementForColumnHead(form, child, root);
    }
  }

  
}
