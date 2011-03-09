package org.opendatakit.aggregate.server;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.ColumnFilterHeader;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.common.Visibility;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.constants.BasicConsts;

public class GenerateHeaderInfo {

  private SubmissionUISummary summary;

  private Form form;

  private FilterGroup filterGroup;

  private List<FormElementModel> keeps;
  private List<FormElementModel> removes;

  private List<FormElementModel> includes;

  public GenerateHeaderInfo(FilterGroup filterGroup, SubmissionUISummary summary, Form form) {
    this.summary = summary;
    this.form = form;
    this.filterGroup = filterGroup;
  }
  
  public List<FormElementModel> getIncludedElements() {
    return includes;
  }
  

  public void processForHeaderInfo(FormElementModel node) {
    
    // check if we need to apply filters
    if (filterGroup != null) {
      includes = new ArrayList<FormElementModel>();
      
      // process filters
      for (Filter filter : filterGroup.getFilters()) {
        if (filter instanceof ColumnFilter) {
          ColumnFilter cf = (ColumnFilter) filter;

          // processing variables
          List<FormElementModel> columns = new ArrayList<FormElementModel>();
          FormElementKey femKey;
          FormElementModel retrievedElement;

          // convert filter to fem
          for (ColumnFilterHeader columnHeader : cf.getColumnFilterHeaders()) {
            String decodeKey = columnHeader.getColumn().getColumnEncoding();
            femKey = new FormElementKey(decodeKey);
            retrievedElement = FormElementModel.retrieveFormElementModel(form, femKey);
            columns.add(retrievedElement);
          }

          // add to appropriate keep or remove
          if (cf.getVisibility().equals(Visibility.KEEP)) {
            addKeepFormElements(columns);
          } else {
            addRemoveFormElements(columns);
          }
        }
      }
    }

    // start the process of generating columns
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
        processFilter(nodeName, node, false);

      }
      break;
    case GEOPOINT:
      processFilter(nodeName, node, true);
      break;
    default:
      processFilter(node.getElementName(), node, false);
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

  private void processFilter(String nodeName, FormElementModel node, boolean geopoint) {
    
    if(includes == null) {
      addNodeToHeader(nodeName, node, geopoint);
      return;
    }
    
    // check to see node is included in filter  
    if(removes != null && keeps != null) {
      if(!keeps.contains(node) && removes.contains(node)) {
        addNodeToHeader(nodeName, node, geopoint);                
      }
    } else if (keeps != null) {
      if(keeps.contains(node)) {
        addNodeToHeader(nodeName, node, geopoint);        
      }      
    } else if (removes != null) {
      if(!removes.contains(node)) {
        addNodeToHeader(nodeName, node, geopoint);        
      }
    } else {
      addNodeToHeader(nodeName, node, geopoint);
    }
  }

  void addNodeToHeader(String nodeName, FormElementModel node, boolean geopoint) {
    FormElementKey key = node.constructFormElementKey(form);
    
    if(geopoint) {
      summary.addSubmissionHeader(nodeName + BasicConsts.COLON + GeoPoint.LATITUDE, key.toString());
      summary.addSubmissionHeader(nodeName + BasicConsts.COLON + GeoPoint.LONGITUDE, key.toString());
      summary.addSubmissionHeader(nodeName + BasicConsts.COLON + GeoPoint.ALTITUDE, key.toString());
      summary.addSubmissionHeader(nodeName + BasicConsts.COLON + GeoPoint.ACCURACY, key.toString());  
    } else {
      summary.addSubmissionHeader(nodeName, key.toString());
    }
    // note: if there is no filter we will get here without an includes created
    if(includes != null) {
      includes.add(node);
    }
  }
  
  private void addKeepFormElements(List<FormElementModel> formElements) {
    if(keeps == null) {
      keeps = new ArrayList<FormElementModel>();
    }
    
    keeps.addAll(formElements);
  }
  
  private void addRemoveFormElements(List<FormElementModel> formElements) {
    if(removes == null) {
      removes = new ArrayList<FormElementModel>();
    }
    
    removes.addAll(formElements);
  }
}
