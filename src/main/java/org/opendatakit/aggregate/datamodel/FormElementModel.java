/*
 * Copyright (C) 2010 University of Washington
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
package org.opendatakit.aggregate.datamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opendatakit.aggregate.constants.common.FormElementNamespace;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.common.web.CallingContext;

/**
 * Wrapper class that presents the submission-level view of a form.
 *
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 *
 */
public final class FormElementModel {

  /* xform element types */
  public static enum ElementType {
    // xform tag types
    STRING, JRDATETIME, JRDATE, JRTIME, INTEGER, DECIMAL, GEOPOINT, BINARY, // identifies
                                                                            // BinaryContent
                                                                            // table
    BOOLEAN, SELECT1, // identifies SelectChoice table
    SELECTN, // identifies SelectChoice table
    REPEAT, GROUP, METADATA
  };

  public static enum Metadata {
    /**
     * NOTE: the order here is the order in which these are listed. DO NOT
     * CHANGE!!!
     */
    META_INSTANCE_ID, META_MODEL_VERSION, META_UI_VERSION, META_SUBMISSION_DATE, META_IS_COMPLETE, META_DATE_MARKED_AS_COMPLETE;

    public String toString() {
      return "*" + this.name().toLowerCase().replaceAll("_", "-") + "*";
    }
  };

  private static final String K_SL = "/";
  private static final String K_COLON = ":";

  private final Metadata type;
  private final FormDataModel fdm;
  private final List<FormElementModel> children = new ArrayList<FormElementModel>();
  private final FormElementModel parent;

  FormElementModel(FormElementModel parent, Metadata type) {
    fdm = null;
    this.parent = parent;
    this.type = type;
  }

  FormElementModel(final FormDataModel fdm, final FormElementModel parent) {
    this.fdm = fdm;
    this.parent = parent;
    this.type = null;
    if (parent == null) {
      // add the instance meta data...
      // the form meta data is not part of the submission form element model...
      for (Metadata m : Metadata.values()) {
        children.add(new FormElementModel(this, m));
      }
    }

    switch (fdm.getElementType()) {
    // xform tag types
    case STRING:
    case JRDATETIME:
    case JRDATE:
    case JRTIME:
    case INTEGER:
    case DECIMAL:
    case GEOPOINT:
    case BINARY: // identifies BinaryContent table
    case BOOLEAN:
    case SELECT1: // identifies SelectChoice table
    case SELECTN: // identifies SelectChoice table
      // no children for these...
      break;
    case GROUP:
    case REPEAT:
      // these have children...
      for (FormDataModel f : fdm.getChildren()) {
        addChildHelper(f);
      }
      break;
    default:
      throw new IllegalStateException("Unexpectedly traversing hidden datatypes");
    }
  }

  private final void addChildHelper(FormDataModel f) {
    switch (f.getElementType()) {
    // xform tag types
    case STRING:
    case JRDATETIME:
    case JRDATE:
    case JRTIME:
    case INTEGER:
    case DECIMAL:
    case GEOPOINT:
    case BINARY: // identifies BinaryContent table
    case BOOLEAN:
    case SELECT1: // identifies SelectChoice table
    case SELECTN: // identifies SelectChoice table
    case GROUP:
    case REPEAT:
      // these are individual children...
      children.add(new FormElementModel(f, this));
      break;
    case PHANTOM:
      // phantom children are ours!
      for (FormDataModel ff : f.getChildren()) {
        addChildHelper(ff);
      }
      break;
    default:
      throw new IllegalStateException("Unexpectedly traversing hidden datatypes");
    }
  }

  public final boolean equals(Object obj) {
    if ((obj == null) || !(obj instanceof FormElementModel))
      return false;
    FormElementModel ref = (FormElementModel) obj;

    return ((fdm == null) ? (ref.fdm == null) : ((ref.fdm != null) && fdm.equals(ref.fdm)))
        && ((parent == null) ? (ref.parent == null) : ((ref.parent != null) && parent
            .equals(ref.parent))) && (type == ref.type);
  }

  /**
   * Returns the list of children that are in the specified form element
   * namespaces.
   *
   * @param namespaces
   *          -- collection of form element namespaces to filter against.
   * @return list of children contained in the collection of namespaces.
   */
  public final List<FormElementModel> getChildren(Collection<FormElementNamespace> namespaces) {
    List<FormElementModel> subset = new ArrayList<FormElementModel>();
    boolean hasMetadata = false;
    boolean hasValues = false;

    if (namespaces == null) {
      // no restriction -- return everything
      return children;
    }

    for (FormElementNamespace ns : namespaces) {
      hasMetadata = hasMetadata || (ns == FormElementNamespace.METADATA);
      hasValues = hasValues || (ns == FormElementNamespace.VALUES);
    }

    if (hasValues && hasMetadata) {
      // want both -- return everything
      return children;
    }

    if (!hasValues && !hasMetadata) {
      return subset; // empty subset...
    }

    // do the hard work...
    for (FormElementModel child : children) {
      if (child.isMetadata() && hasMetadata) {
        subset.add(child);
      } else if (!child.isMetadata() && hasValues) {
        subset.add(child);
      }
    }
    return subset;
  }

  public final List<FormElementModel> getChildren() {
    return children;
  }

  public final FormElementModel getParent() {
    return parent;
  }

  public final FormDataModel getFormDataModel() {
    return fdm;
  }

  public final Metadata getType() {
    return type;
  }

  public final String getElementName() {
    if (fdm == null) {
      return type.toString();
    }
    return fdm.getElementName();
  }

  public final String toString() {
    return getElementName();
  }

  public final boolean isMetadata() {
    return (fdm == null);
  }

  public final ElementType getElementType() {
    if (fdm == null) {
      switch (type) {
      case META_INSTANCE_ID:
        return ElementType.STRING;
      case META_SUBMISSION_DATE:
        return ElementType.JRDATETIME;
      case META_UI_VERSION:
        return ElementType.INTEGER;
      case META_MODEL_VERSION:
        return ElementType.INTEGER;
      case META_IS_COMPLETE:
        return ElementType.BOOLEAN;
      case META_DATE_MARKED_AS_COMPLETE:
        return ElementType.JRDATETIME;
      default:
        throw new IllegalStateException("unhandled metadata type");
      }
    }
    switch (fdm.getElementType()) {
    // xform tag types
    case STRING:
      return ElementType.STRING;
    case JRDATETIME:
      return ElementType.JRDATETIME;
    case JRDATE:
      return ElementType.JRDATE;
    case JRTIME:
      return ElementType.JRTIME;
    case INTEGER:
      return ElementType.INTEGER;
    case DECIMAL:
      return ElementType.DECIMAL;
    case GEOPOINT:
      return ElementType.GEOPOINT;
    case BINARY: // identifies BinaryContent table
      return ElementType.BINARY;
    case BOOLEAN:
      return ElementType.BOOLEAN;
    case SELECT1: // identifies SelectChoice table
      return ElementType.SELECT1;
    case SELECTN: // identifies SelectChoice table
      return ElementType.SELECTN;
    case GROUP:
      return ElementType.GROUP;
    case REPEAT:
      return ElementType.REPEAT;
    default:
      throw new IllegalStateException("hidden type exists within FormElementModel!");
    }
  }

  public boolean depthFirstTraversal(FormElementModelVisitor visitor, CallingContext cc) {
    ElementType type = getElementType();

    if (type == ElementType.GROUP) {

      // test short-circuit entry guard...
      if (visitor.enter(this, cc))
        return true;

      try {
        // recurse on each value within
        for (FormElementModel value : getChildren()) {
          if (value.depthFirstTraversal(visitor, cc))
            return true;
        }
      } finally {
        // depart from group
        visitor.leave(this, cc);
      }

    } else if (type == ElementType.REPEAT) {

      // test short-circuit entry guard...
      if (visitor.enter(this, cc))
        return true;

      try {
        // descend into each concrete instance within the repeat group
        int ordinal = 0;
        while (visitor.descendIntoRepeat(this, ++ordinal, cc)) {

          try {
            // within this concrete instance, recurse on each value within
            for (FormElementModel value : getChildren()) {
              if (value.depthFirstTraversal(visitor, cc))
                return true;
            }
          } finally {
            // ascend from the concrete instance
            visitor.ascendFromRepeat(this, ordinal, cc);
          }
        }
      } finally {
        // depart from this repeat group
        visitor.leave(this, cc);
      }

    } else {

      // simple type -- traverse and apply short-circuit logic...
      if (visitor.traverse(this, cc))
        return true;
    }

    return false;
  }

  public final String getGroupQualifiedElementName() {
    if (fdm == null) {
      return type.toString();
    }
    return fdm.getGroupQualifiedElementName();
  }

  /**
   * Used internally to construct the abstract XPath defining this
   * FormElementModel. This should have a parallel structure to the
   * SubmissionKey, which uses the getGroupQualifiedElementName() (above) and
   * the collapsed entity sets of the SubmissionSet for its construction (i.e.,
   * they are different construction methodologies, so there is a risk of
   * divergence).
   *
   * @param form
   * @return string representation of this FormElementModel
   */
  private final String getFullyQualifiedElementName(IForm form) {

    StringBuilder b = new StringBuilder();
    if (getParent() == null) {
      b.append(form.getFormId());
      b.append(K_SL);
      b.append(getElementName());
    } else {
      b.append(getParent().getFullyQualifiedElementName(form));
      if (getParent().getElementType() == ElementType.REPEAT || getParent().getParent() == null) {
        // repeat or top level group
        b.append(K_SL);
      } else {
        // intermediate group
        b.append(K_COLON);
      }
      b.append(getElementName());
    }
    return b.toString();
  }

  /**
   * Construct the FormElementKey that holds the abstract XPath expression
   * defining this FormElementModel.
   *
   * @param form
   *          the form containing this FormElementModel
   * @return the FormElementKey describing the FormElementModel
   */
  public final FormElementKey constructFormElementKey(IForm form) {
    return new FormElementKey(getFullyQualifiedElementName(form));
  }

  /**
   * Search the children of this element and return the one that has the name
   * that matches.
   *
   * @param elementName
   *          - name to match
   * @return the element or null if not found.
   */
  public final FormElementModel findElementByName(String elementName) {
    if (elementName == null) {
      throw new IllegalArgumentException("null elementName passed in!");
    }

    for (FormElementModel m : children) {
      if (m.getElementName().equals(elementName)) {
        return m;
      }
    }
    return null;
  }

  // //////////////////////////////////////////////////////////////////////////////
  // Static methods
  // //////////////////////////////////////////////////////////////////////////////

  /**
   * Given a form definition and a FormElementKey, retrieves the
   * FormElementModel corresponding to the key.
   *
   * @param form
   * @param key
   * @return the form element model corresponding to the key.
   * @throws IllegalArgumentException
   */
  public static final FormElementModel retrieveFormElementModel(IForm form, FormElementKey key) {
    String[] slashParts = key.toString().split(K_SL);
    int slashPosition = 0;
    if (!form.getFormId().equals(slashParts[slashPosition])) {
      throw new IllegalArgumentException("FormElementKey is not appropriate for this Form");
    }
    ++slashPosition;
    FormElementModel currentElement = form.getTopLevelGroupElement();
    boolean first = true;
    while (slashParts.length > slashPosition) {
      String[] colonParts = slashParts[slashPosition].split(K_COLON);
      int colonPosition = 0;
      if (first) {
        // first time through, the top level group element is the first colon
        // part
        // all other times, we need to search to find a match for this first
        // part.
        if (!currentElement.getElementName().equals(colonParts[colonPosition])) {
          throw new IllegalArgumentException("FormElementKey is not well formed!");
        }
        ++colonPosition;
        first = false;
      }
      while (colonParts.length > colonPosition) {
        boolean found = false;
        for (FormElementModel m : currentElement.getChildren()) {
          if (m.getElementName().equals(colonParts[colonPosition])) {
            found = true;
            currentElement = m;
            break;
          }
        }
        if (!found) {
          throw new IllegalArgumentException("FormElementKey is not well formed!");
        }
        ++colonPosition;
      }
      ++slashPosition;
    }
    return currentElement;
  }

  public static final FormElementModel buildFormElementModelTree(FormDataModel topLevelGroup) {
    return new FormElementModel(topLevelGroup, null);
  }
}
