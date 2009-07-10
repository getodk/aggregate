/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.form;

import com.google.appengine.api.datastore.Key;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
/**
 * Persistable definition of a key/value pair that is
 * based on repeatable form elements. The key is the
 * elementName and the value is the datastore key to the formElement
 *
 * @author wbrunette@gmail.com
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class FormRepeat {
  @SuppressWarnings("unused") // used by JDO
  @Persistent
  private Form form;

  @SuppressWarnings("unused") // used by JDO
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key primaryKey;

  @Persistent
  private String repeatElementName;
  
  @Persistent
  private Key elementKey;
  
  public FormRepeat(String repeatElementName, Key elementKey) {
    this.repeatElementName = repeatElementName;
    this.elementKey = elementKey;
  }
  
  public String getRepeatElementName() {
    return repeatElementName;
  }
  
  public FormElement getFormElement(PersistenceManager pm) {
    return pm.getObjectById(FormElement.class, elementKey);
  }
}
