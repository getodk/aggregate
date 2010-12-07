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
package org.opendatakit.aggregate.task;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.table.CsvFormatter;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public abstract class AbstractCsvGeneratorImpl implements CsvGenerator{

  public void generatorCsv(Form form, String baseWebServerUrl, Datastore ds, User user) throws ODKFormNotFoundException, ODKDatastoreException, ODKIncompleteSubmissionData {

    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    PrintWriter pw = new PrintWriter(stream);
   
    // create CSV
    QueryByDate query = new QueryByDate(form, BasicConsts.EPOCH, false, ServletConsts.FETCH_LIMIT,
        ds, user);
    SubmissionFormatter formatter = new CsvFormatter(form, baseWebServerUrl, pw, null);
    formatter.processSubmissions(query.getResultSubmissions());

    // TODO save out to binary
  }

}
