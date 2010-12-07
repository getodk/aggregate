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

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.task.AbstractCsvGeneratorImpl;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class CsvGeneratorImpl extends AbstractCsvGeneratorImpl implements Runnable {

  private Form form;
  private String baseWebServerUrl;
  private User user;

  @Override
  public void createCsvTask(Form form, String baseServerWebUrl, User user) {
    this.form = form;
    this.baseWebServerUrl = baseServerWebUrl;
    this.user = user;
    AggregrateThreadExecutor exec = AggregrateThreadExecutor.getAggregateThreadExecutor();
    exec.execute(this);
  }

  @Override
  public void run() {
    Datastore ds = (Datastore) ContextFactory.get().getBean(BeanDefs.DATASTORE_BEAN);
    try {
      generatorCsv(form, baseWebServerUrl, ds, user);
    } catch (Exception e) {
      e.printStackTrace();
      // TODO: PROBLEM - figure out how we are going to restart it
    }

  }
}
