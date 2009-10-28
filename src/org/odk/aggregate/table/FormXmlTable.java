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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.servlet.FormXmlServlet;

import com.google.appengine.api.datastore.KeyFactory;

/**
 * Generates an xml description of forms for the servlets
 * 
 * @author wbrunette@gmail.com
 *
 */
public class FormXmlTable {

  private String baseServerUrl;

  public FormXmlTable(String serverName) {
    this.baseServerUrl = HtmlUtil.createUrl(serverName) + FormXmlServlet.ADDR;
  }

  private String generateFormXmlEntry(String odkFormKey, String formName) {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put(ServletConsts.ODK_FORM_KEY, odkFormKey);
    String urlLink = HtmlUtil.createLinkWithProperties(baseServerUrl, properties);
    return HtmlConsts.BEGIN_OPEN_TAG + TableConsts.FORM_TAG + BasicConsts.SPACE
        + HtmlUtil.createAttribute(TableConsts.URL_ATTR, urlLink) 
        + HtmlConsts.END_TAG + formName + TableConsts.END_FORM_TAG;
  }

  public String generateXmlListOfForms() {
    EntityManager em = EMFactory.get().createEntityManager();
    String xml = TableConsts.BEGIN_FORMS_TAG + BasicConsts.NEW_LINE;
    try {
      Query formQuery = em.createQuery("select from Form");
      @SuppressWarnings("unchecked")
      List<Form> forms = formQuery.getResultList();

      // build HTML table of form information
      for (Form form : forms) {
        xml +=
            generateFormXmlEntry(KeyFactory.keyToString(form.getKey()), form.getViewableName())
                + BasicConsts.NEW_LINE;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      em.close();
    }

    return xml + TableConsts.END_FORMS_TAG;
  }

}
