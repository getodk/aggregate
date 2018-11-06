/*
 * Copyright (C) 2011 University of Washington.
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

import java.util.List;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.submission.SubmissionField;
import org.opendatakit.aggregate.submission.SubmissionVisitor;
import org.opendatakit.common.datamodel.BinaryContentManipulator.BlobSubmissionOutcome;
import org.opendatakit.common.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.web.CallingContext;

public abstract class MetadataBaseType<T> implements SubmissionField<T> {
  /**
   * Backing object holding the value of the submission field
   */
  protected final DynamicCommonFieldsBase backingObject;
  protected final DataField field;
  protected final FormElementModel metadataType;

  MetadataBaseType(DynamicCommonFieldsBase backingObject, FormElementModel metadataType,
                   DataField field) {
    this.backingObject = backingObject;
    this.field = field;
    this.metadataType = metadataType;
  }

  @Override
  public final FormElementModel getFormElementModel() {
    return metadataType;
  }

  @Override
  public void getValueFromEntity(CallingContext cc) {
    // no-op
  }

  @Override
  public void recursivelyAddEntityKeysForDeletion(List<EntityKey> keyList, CallingContext cc) {
    // no-op
  }

  @Override
  public void persist(CallingContext cc) {
    // no-op
  }

  @Override
  public String getPropertyName() {
    return metadataType.toString();
  }

  @Override
  public boolean depthFirstTraversal(SubmissionVisitor visitor) {
    return visitor.traverse(this);
  }

  @Override
  public boolean isBinary() {
    return false;
  }

  @Override
  public void setValueFromString(String value) {
    throw new IllegalStateException("unexpected call to setValueFromString");
  }

  @Override
  public BlobSubmissionOutcome setValueFromByteArray(byte[] byteArray, String contentType,
                                                     String unrootedFilePath, boolean overwriteOK, CallingContext cc) {
    throw new IllegalStateException("unexpected call to setValueFromByteArray");
  }

}
