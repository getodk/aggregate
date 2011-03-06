package org.opendatakit.aggregate.server;

import java.util.List;

import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.constants.BasicConsts;

public class GenerateHeaderInfo {

  private SubmissionUISummary summary;

  public GenerateHeaderInfo(SubmissionUISummary summary) {
    this.summary = summary;
  }

  public void processForHeaderInfo(FormElementModel node) {
    processElementForColumnHead(node, node, BasicConsts.EMPTY_STRING);
  }
  
  /**
   * Helper function to recursively go through the element tree and create the
   * column headings
   * 
   */
  private void processElementForColumnHead(FormElementModel node, FormElementModel root,
      String parentName) {
    if (node == null)
      return;

    String nodeName = parentName + node.getElementName();
    String revisedParentName = parentName;

    switch (node.getElementType()) {
    case GROUP:
      if (node != root) {
        // else skip and goto children as we do not know how to display
        // append parent name incase embedded tag
        revisedParentName = revisedParentName + node.getElementName() + BasicConsts.COLON;
      }
      break;
    case REPEAT:
      if (node == root) {
        // we are processing this as a group...
        List<FormElementModel> childDataElements = node.getChildren();
        for (FormElementModel child : childDataElements) {
          processElementForColumnHead(child, root, revisedParentName);
        }
      } else {
        // we are processing this as a table element
        addHeader(nodeName, node);

      }
    case GEOPOINT:
      addHeader(nodeName + BasicConsts.COLON + GeoPoint.LATITUDE, node);
      addHeader(nodeName + BasicConsts.COLON + GeoPoint.LONGITUDE, node);
      addHeader(nodeName + BasicConsts.COLON + GeoPoint.ALTITUDE, node);
      addHeader(nodeName + BasicConsts.COLON + GeoPoint.ACCURACY, node);
      break;
    default:
      addHeader(node.getElementName(), node);
    }

    // only recurse into the elements that are not binary, geopoint,
    // repeat or choice elements
    if ((node.getElementType() != ElementType.BINARY)
        && (node.getElementType() != ElementType.REPEAT)
        && (node.getElementType() != ElementType.GEOPOINT)
        && (node.getElementType() != ElementType.SELECT1)
        && (node.getElementType() != ElementType.SELECTN)) {

      List<FormElementModel> childDataElements = node.getChildren();
      for (FormElementModel child : childDataElements) {
        processElementForColumnHead(child, root, revisedParentName);
      }
    }
  }

  public void addHeader(String nodeName, FormElementModel node) {
    summary.addSubmissionHeader( nodeName, node.getElementName());
  }
}
