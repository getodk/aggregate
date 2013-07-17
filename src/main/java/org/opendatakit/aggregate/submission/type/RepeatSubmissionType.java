/*
 * Copyright (C) 2009 Google Inc. 
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.aggregate.submission.type;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.submission.SubmissionElement;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.SubmissionVisitor;
import org.opendatakit.common.datamodel.DynamicBase;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.Direction;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

/**
 * Data Storage type for a repeat type. Store a list of datastore keys to
 * submission sets in an entity
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class RepeatSubmissionType implements SubmissionRepeat {

  /**
   * ODK identifier that uniquely identifies the form
   */
  private final IForm form;

  /**
   * Enclosing submission set
   */
  private final SubmissionSet enclosingSet;

  /**
   * Identifier for repeat
   */
  private final FormElementModel repeatGroup;

  private final String uriAssociatedRow;
  /**
   * List of submission sets that are a part of this submission set Ordered by
   * OrdinalNumber...
   */
  private List<SubmissionSet> submissionSets = new ArrayList<SubmissionSet>();

  public RepeatSubmissionType(SubmissionSet enclosingSet, FormElementModel repeatGroup,
      String uriAssociatedRow, IForm form) {
    this.enclosingSet = enclosingSet;
    this.form = form;
    this.repeatGroup = repeatGroup;
    this.uriAssociatedRow = uriAssociatedRow;
  }

  @Override
  public final FormElementModel getFormElementModel() {
    return repeatGroup;
  }
  
  public SubmissionSet getEnclosingSet() {
    return enclosingSet;
  }

  public String getUniqueKeyStr() {
    EntityKey key = enclosingSet.getKey();
    return key.getKey();
  }

  public void addSubmissionSet(SubmissionSet submissionSet) {
    submissionSets.add(submissionSet);
  }

  public List<SubmissionSet> getSubmissionSets() {
    return submissionSets;
  }

  public int getNumberRepeats() {
    return submissionSets.size();
  }

  /**
   * @return submissionKey that defines all the repeats for this particular
   *         repeat group.
   */
  public SubmissionKey constructSubmissionKey() {
    return enclosingSet.constructSubmissionKey(repeatGroup);
  }

  /**
   * Format value for output
   * 
   * @param elemFormatter
   *          the element formatter that will convert the value to the proper
   *          format for output
   */
  @Override
  public void formatValue(ElementFormatter elemFormatter, Row row, String ordinalValue,
      CallingContext cc) throws ODKDatastoreException {
    elemFormatter.formatRepeats(this, repeatGroup, row, cc);
  }

  @Override
  public void getValueFromEntity(CallingContext cc) throws ODKDatastoreException {

    DynamicBase rel = (DynamicBase) repeatGroup.getFormDataModel().getBackingObjectPrototype();

    Query q = cc.getDatastore().createQuery(rel, "RepeatSubmissionType.getValueFromEntity", cc.getCurrentUser());
    q.addFilter(rel.parentAuri, FilterOperation.EQUAL, uriAssociatedRow);
    q.addSort(rel.parentAuri, Direction.ASCENDING); // for GAE work-around
    q.addSort(rel.ordinalNumber, Direction.ASCENDING);

    // reconstruct all the repeating groups from a single submission.
    // This should be a small number. We don't have the logic to
    // handle fractional returns of rows.
    List<? extends CommonFieldsBase> repeatGroupList = q.executeQuery();
    for (CommonFieldsBase cb : repeatGroupList) {
      DynamicBase d = (DynamicBase) cb;
      SubmissionSet set = new SubmissionSet(enclosingSet, d, repeatGroup, form, cc);
      submissionSets.add(set);
    }
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof RepeatSubmissionType)) {
      return false;
    }

    RepeatSubmissionType other = (RepeatSubmissionType) obj;
    return form.equals(other.form) && repeatGroup.equals(other.repeatGroup)
        && submissionSets.equals(other.submissionSets);
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 13;

    hashCode += form.hashCode();
    hashCode += repeatGroup.hashCode();
    hashCode += submissionSets.hashCode();

    return hashCode;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = enclosingSet.constructSubmissionKey(repeatGroup) + "\n";
    for (SubmissionSet set : submissionSets) {
      str += FormatConsts.TO_STRING_DELIMITER + set.toString();
    }
    return str;
  }

  @Override
  public void recursivelyAddEntityKeys(List<EntityKey> keyList, CallingContext cc)
      throws ODKDatastoreException {
    for (SubmissionSet s : submissionSets) {
      s.recursivelyAddEntityKeys(keyList, cc);
    }
  }

  @Override
  public void persist(CallingContext cc) throws ODKEntityPersistException, ODKOverQuotaException {
    for (SubmissionSet s : submissionSets) {
      s.persist(cc);
    }
  }

  @Override
  public FormElementModel getElement() {
    return repeatGroup;
  }

  @Override
  public String getPropertyName() {
    return repeatGroup.getElementName();
  }

  @Override
  public boolean depthFirstTraversal(SubmissionVisitor visitor) {
    if (visitor.traverse(this))
      return true;

    for (SubmissionSet s : submissionSets) {
      if (s.depthFirstTraversal(visitor))
        return true;
    }
    return false;
  }

  public List<SubmissionValue> findElementValue(FormElementModel element) {
    List<SubmissionValue> values = new ArrayList<SubmissionValue>();

    for (SubmissionSet s : submissionSets) {
      values.addAll(s.findElementValue(element));
    }
    return values;
  }

  @Override
  public SubmissionElement resolveSubmissionKeyBeginningAt(int i, List<SubmissionKeyPart> parts) {
    SubmissionKeyPart p = parts.get(i);

    Long ordinalNumber = p.getOrdinalNumber();
    if (ordinalNumber != null) {
      return submissionSets.get(ordinalNumber.intValue() - 1).resolveSubmissionKeyBeginningAt(i,
          parts);
    }

    String auri = p.getAuri();
    if (auri == null) {
      return this; // they want the repeat group...
    }

    for (SubmissionSet s : submissionSets) {
      if (s.getKey().getKey().equals(auri)) {
        return s.resolveSubmissionKeyBeginningAt(i, parts);
      }
    }
    return null;
  }

}
