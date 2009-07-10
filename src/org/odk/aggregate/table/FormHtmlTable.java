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

package org.odk.aggregate.table;

import com.google.appengine.api.datastore.KeyFactory;

import org.odk.aggregate.PMFactory;
import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.servlet.CsvServlet;
import org.odk.aggregate.servlet.FormSubmissionsServlet;
import org.odk.aggregate.servlet.FormXmlServlet;
import org.odk.aggregate.servlet.SubmissionServlet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Generates an html table of forms for the servlets
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class FormHtmlTable {
  private static List<String> HEADERS =
      Arrays.asList(TableConsts.FT_HEADER_NAME, TableConsts.FT_HEADER_ODKID,
          TableConsts.FT_HEADER_USER, TableConsts.FT_HEADER_RESULTS, TableConsts.FT_HEADER_CSV,
          TableConsts.FT_HEADER_XFORM, TableConsts.FT_HEADER_SUBMISSION);

  /**
   * Generate a table of available forms in html format
   * 
   * @return html table of available forms
   */
  public String generateHtmlFormTable() {

    PersistenceManager pm = PMFactory.get().getPersistenceManager();
    ResultTable results = new ResultTable(HEADERS);

    try {
      Query formQuery = pm.newQuery(Form.class);
      @SuppressWarnings("unchecked")
      List<Form> forms = (List<Form>) formQuery.execute();

      // build HTML table of form information
      for (Form form : forms) {

        // create buttons
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(ServletConsts.ODK_ID, form.getOdkId());
        String resultButton =
            HtmlUtil.createHtmlButtonToGetServlet(FormSubmissionsServlet.ADDR,
                TableConsts.RESULTS_BUTTON_TXT, properties);
        String csvButton = HtmlUtil.createHtmlButtonToGetServlet(CsvServlet.ADDR, 
                TableConsts.CSV_BUTTON_TXT, properties);

        Map<String, String> xmlProperties = new HashMap<String, String>();
        xmlProperties.put(ServletConsts.ODK_FORM_KEY, KeyFactory.keyToString(form.getKey()));
        xmlProperties.put(ServletConsts.HUMAN_READABLE, BasicConsts.TRUE);
        String xmlButton =
            HtmlUtil.createHtmlButtonToGetServlet(FormXmlServlet.ADDR, TableConsts.XML_BUTTON_TXT,
                xmlProperties);

        Map<String, String> submissionProperties = new HashMap<String, String>();
        submissionProperties.put(ServletConsts.ODK_FORM_KEY, KeyFactory.keyToString(form.getKey()));
        String submissionButton =
            HtmlUtil.createHtmlButtonToGetServlet(SubmissionServlet.ADDR,
                TableConsts.SUBMISSION_BUTTON_TXT, submissionProperties);

        // create row
        int index = 0;
        String[] row = new String[results.getNumColumns()];

        row[index++] = form.getViewableName();
        row[index++] = form.getOdkId();
        row[index++] = form.getCreationUser();
        row[index++] = resultButton;
        row[index++] = csvButton;
        row[index++] = xmlButton;
        row[index++] = submissionButton;

        results.addRow(row);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      pm.close();
    }

    return HtmlUtil.wrapResultTableWithHtmlTags(results);
  }


}
