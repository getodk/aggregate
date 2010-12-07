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
package org.opendatakit.aggregate.task.tomcat;

import org.opendatakit.aggregate.exception.ODKExternalServiceDependencyException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.task.AbstractFormDeleteImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormDeleteImpl extends AbstractFormDeleteImpl implements Runnable {

  private Form form;
  private User user;
  
  @Override
  public final void createFormDeleteTask(Form form, User user) throws ODKDatastoreException, ODKFormNotFoundException, ODKExternalServiceDependencyException {
    this.form = form;
    this.user = user;
    AggregrateThreadExecutor exec = AggregrateThreadExecutor.getAggregateThreadExecutor();
    exec.execute(this);
  }

  @Override
  public void run() {
    try {
      deleteForm(form, user);
    } catch (Exception e) {
      e.printStackTrace();
      // TODO: PROBLEM - figure out how we are going to restart it 
    }
    
  }

}
