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


import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel.ElementType;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.submission.SubmissionField;
import org.opendatakit.aggregate.submission.SubmissionVisitor;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public abstract class SubmissionFieldBase<T> implements SubmissionField<T> {

  protected final FormElementModel element;

  public SubmissionFieldBase(FormElementModel element) {
    this.element = element;
  }

  @Override
  public final String getPropertyName() {
    return element.getElementName();
  }

  @Override
  public final FormElementModel getFormElementModel() {
    return element;
  }

  @Override
  public boolean depthFirstTraversal(SubmissionVisitor visitor) {
    return visitor.traverse(this);
  }

  public abstract T getValue();

  public abstract void setValueFromString(String value) throws ODKConversionException;


  public abstract void getValueFromEntity(CallingContext cc) throws ODKDatastoreException;

  public abstract void formatValue(ElementFormatter elemFormatter, Row row, String ordinalValue, CallingContext cc) throws ODKDatastoreException;

  @Override
  public final boolean isBinary() {
    return (element.getFormDataModel().getElementType() == ElementType.REF_BLOB);
  }

  @Override
  public BinaryContentManipulator.BlobSubmissionOutcome setValueFromByteArray(byte[] byteArray, String contentType, String unrootedFilePath, boolean overwriteOK, CallingContext cc) throws ODKDatastoreException {
    if (isBinary()) {
      throw new IllegalStateException("Should be overridden in derived class");
    } else {
      throw new IllegalStateException(ErrorConsts.BINARY_ERROR);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SubmissionFieldBase<?>)) {
      return false;
    }

    SubmissionFieldBase<?> other = (SubmissionFieldBase<?>) obj;
    return (element == null ? (other.element == null) :
        (other.element != null && element.equals(other.element)));
  }

  @Override
  public int hashCode() {
    int hashCode = 13;
    if (element != null) hashCode += element.hashCode();
    return hashCode;
  }

  @Override
  public String toString() {
    return (element != null ? element.getElementName() : BasicConsts.EMPTY_STRING);
  }
}
