/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.common.FormElementNamespace;
import org.opendatakit.aggregate.constants.common.GeoPointConsts;
import org.opendatakit.aggregate.constants.common.Visibility;
import org.opendatakit.aggregate.datamodel.FormElementKey;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.web.constants.BasicConsts;

public class GenerateHeaderInfo {

  private SubmissionUISummary summary;

  private IForm form;

  private FilterGroup filterGroup;

  private List<FormElementModel> keeps;
  private List<FormElementModel> removes;

  private List<FormElementModel> includes;

  private Map<FormElementModel, GeopointColumn> geopointFlags;

  public GenerateHeaderInfo(FilterGroup filterGroup, SubmissionUISummary summary, IForm form) {
    this.summary = summary;
    this.form = form;
    this.filterGroup = filterGroup;
  }

  public List<FormElementNamespace> includedFormElementNamespaces() {
    List<FormElementNamespace> namespaces = new ArrayList<FormElementNamespace>();
    namespaces.add(FormElementNamespace.VALUES);
    if (filterGroup != null) {
      if (filterGroup.getIncludeMetadata()) {
        namespaces.add(FormElementNamespace.METADATA);
      }
    }

    return namespaces;
  }

  public List<FormElementModel> getIncludedElements() {
    return includes;
  }

  public Map<String, GeopointHeaderIncludes> getGeopointIncludes() {
    Map<String, GeopointHeaderIncludes> gpsIncludes = new HashMap<String, GeopointHeaderIncludes>();

    if (geopointFlags != null) {
      for (GeopointColumn gpsColumns : geopointFlags.values()) {
        GeopointHeaderIncludes include = new GeopointHeaderIncludes(gpsColumns.getElementName(),
            gpsColumns.includeLatitude(), gpsColumns.includeLongitude(),
            gpsColumns.includeAltitude(), gpsColumns.includeAccuracy());
        gpsIncludes.put(gpsColumns.getElementName(), include);
      }
    }

    return gpsIncludes;
  }

  public void processForHeaderInfo(FormElementModel node) {

    // check if we need to apply filters
    if (filterGroup != null) {
      includes = new ArrayList<FormElementModel>();

      // process filters
      for (Filter filter : filterGroup.getFilters()) {
        if (filter instanceof ColumnFilter) {
          ColumnFilter cf = (ColumnFilter) filter;

          // convert filter to fem
          for (Column columnHeader : cf.getColumnFilterHeaders()) {
            String decodeKey = columnHeader.getColumnEncoding();
            FormElementKey femKey = new FormElementKey(decodeKey);
            FormElementModel fem = FormElementModel.retrieveFormElementModel(form, femKey);

            if (!filterGroup.getIncludeMetadata() && fem.isMetadata()) {
              continue;
            }

            // add to appropriate keep or remove
            if (cf.getVisibility().equals(Visibility.DISPLAY)) {
              addKeepFormElement(fem);
            } else {
              addRemoveFormElement(fem);
            }

            if (fem.getElementType().equals(ElementType.GEOPOINT)) {
              if (geopointFlags == null) {
                geopointFlags = new HashMap<FormElementModel, GeopointColumn>();
              }

              GeopointColumn geopoint = geopointFlags.get(fem);
              if (geopoint == null) {
                geopoint = new GeopointColumn(fem);
                geopointFlags.put(fem, geopoint);
              }

              Long gpsColumnIndex = columnHeader.getGeopointColumnCode();

              // add to appropriate keep or remove
              if (cf.getVisibility().equals(Visibility.DISPLAY)) {
                geopoint.keepColumn(gpsColumnIndex);
              } else {
                geopoint.removeColumn(gpsColumnIndex);
              }

            }
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
          processFilter(nodeName, node);

        }
        break;
      default:
        processFilter(nodeName, node);
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

  private void processFilter(String nodeName, FormElementModel node) {

    if (filterGroup != null) {
      if (!filterGroup.getIncludeMetadata() && node.isMetadata()) {
        return;
      }
    }

    if (includes == null) {
      addNodeToHeader(nodeName, node);
      return;
    }

    // check to see node is included in filter
    if (removes != null && keeps != null) {
      if (keeps.contains(node) && !removes.contains(node)) {
        addNodeToHeader(nodeName, node);
      } else if (keeps.contains(node) && node.getElementType().equals(ElementType.GEOPOINT)) {
        addNodeToHeader(nodeName, node);
      }
    } else if (keeps != null) {
      if (keeps.contains(node)) {
        addNodeToHeader(nodeName, node);
      }
    } else if (removes != null) {
      if (!removes.contains(node) || node.getElementType().equals(ElementType.GEOPOINT)) {
        addNodeToHeader(nodeName, node);
      }
    } else {
      addNodeToHeader(nodeName, node);
    }
  }

  void addNodeToHeader(String nodeName, FormElementModel node) {
    FormElementKey key = node.constructFormElementKey(form);

    if (node.getElementType().equals(ElementType.GEOPOINT)) {
      GeopointColumn gpsColumns = null;

      if (geopointFlags != null) {
        gpsColumns = geopointFlags.get(node);
      }

      if (gpsColumns == null) {
        summary.addGeopointHeader(nodeName + BasicConsts.COLON + GeoPoint.LATITUDE, key.toString(),
            Long.valueOf(GeoPointConsts.GEOPOINT_LATITUDE_ORDINAL_NUMBER));
        summary.addGeopointHeader(nodeName + BasicConsts.COLON + GeoPoint.LONGITUDE,
            key.toString(), Long.valueOf(GeoPointConsts.GEOPOINT_LONGITUDE_ORDINAL_NUMBER));
        summary.addGeopointHeader(nodeName + BasicConsts.COLON + GeoPoint.ALTITUDE, key.toString(),
            Long.valueOf(GeoPointConsts.GEOPOINT_ALTITUDE_ORDINAL_NUMBER));
        summary.addGeopointHeader(nodeName + BasicConsts.COLON + GeoPoint.ACCURACY, key.toString(),
            Long.valueOf(GeoPointConsts.GEOPOINT_ACCURACY_ORDINAL_NUMBER));
      } else {
        if (gpsColumns.includeLatitude()) {
          summary.addGeopointHeader(nodeName + BasicConsts.COLON + GeoPoint.LATITUDE,
              key.toString(), Long.valueOf(GeoPointConsts.GEOPOINT_LATITUDE_ORDINAL_NUMBER));
        }
        if (gpsColumns.includeLongitude()) {
          summary.addGeopointHeader(nodeName + BasicConsts.COLON + GeoPoint.LONGITUDE,
              key.toString(), Long.valueOf(GeoPointConsts.GEOPOINT_LONGITUDE_ORDINAL_NUMBER));
        }
        if (gpsColumns.includeAltitude()) {
          summary.addGeopointHeader(nodeName + BasicConsts.COLON + GeoPoint.ALTITUDE,
              key.toString(), Long.valueOf(GeoPointConsts.GEOPOINT_ALTITUDE_ORDINAL_NUMBER));
        }
        if (gpsColumns.includeAccuracy()) {
          summary.addGeopointHeader(nodeName + BasicConsts.COLON + GeoPoint.ACCURACY,
              key.toString(), Long.valueOf(GeoPointConsts.GEOPOINT_ACCURACY_ORDINAL_NUMBER));
        }

      }
    } else if (node.getElementType().equals(ElementType.BINARY)) {
      summary.addBinarySubmissionHeader(nodeName, key.toString());
    } else if (node.getElementType().equals(ElementType.REPEAT)) {
      summary.addRepeatSubmissionHeader(nodeName, key.toString());
    } else {
      summary.addSubmissionHeader(nodeName, key.toString());
    }
    // note: if there is no filter we will get here without an includes created
    if (includes != null) {
      includes.add(node);
    }
  }

  private void addKeepFormElement(FormElementModel formElement) {
    if (keeps == null) {
      keeps = new ArrayList<FormElementModel>();
    }

    keeps.add(formElement);
  }

  private void addRemoveFormElement(FormElementModel formElement) {
    if (removes == null) {
      removes = new ArrayList<FormElementModel>();
    }

    removes.add(formElement);
  }

  public class GeopointColumn {
    private FormElementModel fem;

    private boolean keepLatFlag = false;
    private boolean keepLongFlag = false;
    private boolean keepAltFlag = false;
    private boolean keepAccFlag = false;
    private boolean removeLatFlag = false;
    private boolean removeLongFlag = false;
    private boolean removeAltFlag = false;
    private boolean removeAccFlag = false;

    public GeopointColumn(FormElementModel fem) {
      this.fem = fem;
    }

    public String getElementName() {
      return fem.getElementName();
    }

    public void removeColumn(Long columnConst) {
      int ordinal = columnConst.intValue();
      switch (ordinal) {
        case GeoPointConsts.GEOPOINT_LATITUDE_ORDINAL_NUMBER:
          removeLatFlag = true;
          break;
        case GeoPointConsts.GEOPOINT_LONGITUDE_ORDINAL_NUMBER:
          removeLongFlag = true;
          break;
        case GeoPointConsts.GEOPOINT_ALTITUDE_ORDINAL_NUMBER:
          removeAltFlag = true;
          break;
        case GeoPointConsts.GEOPOINT_ACCURACY_ORDINAL_NUMBER:
          removeAccFlag = true;
          break;
        default:
          break;
      }
    }

    private boolean hasRemoves() {
      return removeLatFlag || removeLongFlag || removeAltFlag || removeAccFlag;
    }

    public void keepColumn(Long columnConst) {
      int ordinal = columnConst.intValue();
      switch (ordinal) {
        case GeoPointConsts.GEOPOINT_LATITUDE_ORDINAL_NUMBER:
          keepLatFlag = true;
          break;
        case GeoPointConsts.GEOPOINT_LONGITUDE_ORDINAL_NUMBER:
          keepLongFlag = true;
          break;
        case GeoPointConsts.GEOPOINT_ALTITUDE_ORDINAL_NUMBER:
          keepAltFlag = true;
          break;
        case GeoPointConsts.GEOPOINT_ACCURACY_ORDINAL_NUMBER:
          keepAccFlag = true;
          break;
        default:
          break;
      }
    }

    private boolean hasKeeps() {
      return keepLatFlag || keepLongFlag || keepAltFlag || keepAccFlag;
    }

    public boolean includeLatitude() {
      if (hasKeeps() && hasRemoves()) {
        if (keepLatFlag && !removeLatFlag) {
          return true;
        }
      } else if (hasKeeps()) {
        return keepLatFlag;
      } else if (hasRemoves()) {
        return !removeLatFlag;
      }
      return false;
    }

    public boolean includeLongitude() {
      if (hasKeeps() && hasRemoves()) {
        if (keepLongFlag && !removeLongFlag) {
          return true;
        }
      } else if (hasKeeps()) {
        return keepLongFlag;
      } else if (hasRemoves()) {
        return !removeLongFlag;
      }
      return false;
    }

    public boolean includeAltitude() {
      if (hasKeeps() && hasRemoves()) {
        if (keepAltFlag && !removeAltFlag) {
          return true;
        }
      } else if (hasKeeps()) {
        return keepAltFlag;
      } else if (hasRemoves()) {
        return !removeAltFlag;
      }
      return false;
    }

    public boolean includeAccuracy() {
      if (hasKeeps() && hasRemoves()) {
        if (keepAccFlag && !removeAccFlag) {
          return true;
        }
      } else if (hasKeeps()) {
        return keepAccFlag;
      } else if (hasRemoves()) {
        return !removeAccFlag;
      }
      return false;
    }
  }
}
