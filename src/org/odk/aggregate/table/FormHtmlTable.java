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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.servlet.CsvServlet;
import org.odk.aggregate.servlet.ExternalRepoServlet;
import org.odk.aggregate.servlet.FormSubmissionsServlet;
import org.odk.aggregate.servlet.FormXmlServlet;
import org.odk.aggregate.servlet.KmlSettingsServlet;

import com.google.appengine.api.datastore.KeyFactory;

/**
 * Generates an html table of forms for the servlets
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class FormHtmlTable {
  /**
   * GQL query to get forms
   */
  private static final String FORM_QUERY = "select from Form";
  
  /**
   * Array containing the table header names
   */
  private static List<String> HEADERS =
      Arrays.asList(TableConsts.FT_HEADER_NAME, TableConsts.FT_HEADER_ODKID,
          TableConsts.FT_HEADER_USER, TableConsts.FT_HEADER_RESULTS, TableConsts.FT_HEADER_CSV,
          TableConsts.FT_HEADER_EXTERNAL_SERVICE, TableConsts.FT_HEADER_KML, TableConsts.FT_HEADER_XFORM);

  /**
   * Generate a table of available forms in html format
   * 
   * @return html table of available forms
   */
  public String generateHtmlFormTable() {

    EntityManager em = EMFactory.get().createEntityManager();
    ResultTable results = new ResultTable(HEADERS);

    try {
      Query formQuery = em.createQuery(FORM_QUERY);
      @SuppressWarnings("unchecked")
      List<Form> forms = formQuery.getResultList();

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

        properties = new HashMap<String, String>();
        properties.put(ServletConsts.ODK_FORM_KEY, KeyFactory.keyToString(form.getKey()));
        String externalServiceButton =
            HtmlUtil.createHtmlButtonToGetServlet(ExternalRepoServlet.ADDR, TableConsts.EXTERNAL_SERVICE_BUTTON_TXT,
                properties);
        
        String kmlButton =
          HtmlUtil.createHtmlButtonToGetServlet(KmlSettingsServlet.ADDR, TableConsts.KML_BUTTON_TXT,
              properties);
        
        // create row
        List<String> row = new ArrayList<String>();

        row.add(form.getViewableName());
        row.add(form.getOdkId());
        row.add(form.getCreationUser());
        row.add(resultButton);
        row.add(csvButton);       
        row.add(externalServiceButton);
        row.add(kmlButton);
        row.add(xmlButton);

        results.addRow(row);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      em.close();
    }

    return HtmlUtil.wrapResultTableWithHtmlTags(results);
  }

}
