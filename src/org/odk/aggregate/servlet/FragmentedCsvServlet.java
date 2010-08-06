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

package org.odk.aggregate.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.exception.ODKParseException;
import org.odk.aggregate.form.FormElement;
import org.odk.aggregate.table.FragmentedSubmissionTable;
import org.odk.aggregate.table.ResultTable;
import org.odk.aggregate.table.SubmissionTable;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.apphosting.api.DeadlineExceededException;

/**
 * Servlet to generate a CSV file for download, in parts!
 */
public class FragmentedCsvServlet extends ServletUtilBase {

	private static final long serialVersionUID = 9161862118534323521L;

	private static final Logger log = Logger.getLogger(SubmissionTable.class.getName());
	
   /**
    * Title for generated webpage
    */
   private static final String TITLE_INFO = "Download CSV Dataset Range";
     
   /**
    * URI from base
    */
	public static final String ADDR = "csvFragment";

	private static final int DEFAULT_NUM_ENTRIES = 1000;
	
  /**
   * Handler for HTTP Get request that responds with CSV
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    // verify user is logged in
    if (!verifyCredentials(req, resp)) {
      return;
    }
    // required common parameters
    // form or form element identity
    // -- a forward-slash separated list of identity and group or repeat names
    // that identifies the Form or FormElement to retrieve.  It is a form
    // if the path has one or two entries, otherwise it is a repeat group.
    String odkIdPath = getParameter(req, ServletConsts.ODK_ID);
    
    // optional common parameters
    // for client-side simplicity, if these have "" values, treat them as null
    
    // cursor -- tracks where we resume our record fetch (if missing, we start over)
    String websafeCursorString = getParameter(req, ServletConsts.CURSOR);
    if ( websafeCursorString != null && websafeCursorString.length() == 0 ) {
    	websafeCursorString = null;
    }
    
    // number of records to fetch
    String numEntriesStr = getParameter(req, ServletConsts.NUM_ENTRIES);
    if ( numEntriesStr != null && numEntriesStr.length() == 0 ) {
    	numEntriesStr = null;
    }
    
    EntityManager em = EMFactory.get().createEntityManager();
    PrintWriter out = resp.getWriter();
    
    try {
    	int numEntriesToFetch = 0;
    	try {
    		if ( numEntriesStr == null ) {
    			numEntriesToFetch = DEFAULT_NUM_ENTRIES;
    		} else {
    			numEntriesToFetch = Integer.parseInt(numEntriesStr);
    		}
    	} catch (NumberFormatException e) {
    		throw new ODKParseException("Invalid number of entries parameter", e);
    	}
    	
    	// Pick apart the odkId to identify any specific element references
    	// Element references have [@key="..."] clauses on the element name.
    	// At most one [@key="..."] clause should appear prior to the last element.
    	// That clause will be assumed to be the parent key for the last element's relation.
    	//
    	// e.g., 
    	//  myDataForm/data/repeat1   -- access all rows of repeat1 values.
    	//  myDataForm/data[@key="abc"]/repeat1 -- access all repeat1 values with parent data "abc"
    	//  myDataForm/data/repeat1[@key="abc"] -- access the repeat1 record "abc"
    	//
    	// odkPath ends up being: [ "myDataForm", "data", "repeat1" ]
    	// elementReference is either null, or the key to the record
    	// elementParentKey is the parent key that the record must have.
    	List<String> odkPath = null;
    	Key elementReference = null;
    	Key elementParentKey = null;
    	
    	if ( odkIdPath != null ) {
    		odkPath = Arrays.asList(odkIdPath.split(BasicConsts.FORWARDSLASH));
    		for ( int i = 0 ; i < odkPath.size(); ++i ) {
    			String pathElement = odkPath.get(i);
	    		int idx = pathElement.indexOf(ServletConsts.ELEMENT_REFERENCE_KEY_BEGIN_STRING);
	    		if ( idx != -1 ) {
	    			String pe = pathElement.substring(0,idx);
	    			String key = pathElement.substring(idx+ServletConsts.ELEMENT_REFERENCE_KEY_BEGIN_STRING.length());
	    			if (key.endsWith(ServletConsts.ELEMENT_REFERENCE_KEY_END_STRING)) {
	    				key = key.substring(0,key.length()-ServletConsts.ELEMENT_REFERENCE_KEY_END_STRING.length());// remove trailing ]
	    			}
	    			// patch up the path to remove the element reference
	    			odkPath.set(i, pe);
	    			if ( i == odkPath.size()-1 ) {
	    				elementReference = KeyFactory.stringToKey(key);
	    			} else if ( elementParentKey == null ){
	    				elementParentKey = KeyFactory.stringToKey(key);
	    			} else {
	    				throw new ODKParseException("only one non-terminal [@key=...] criteria allowed in query."); 
	    			}
	    		}
    		}
    	}
    	
        if (odkPath != null && odkPath.size() > 2 && numEntriesToFetch > 0) {
        	// repeating groups...
        	// reworked from formmultiplevalueservlet.java
        	
        	// construct the key for the nested reference...
        	Key elementKey = FormElement.constructKey(odkPath);
        	// kind is unfortunately not elementKey.getName()... the parts are swapped.
        	String kind = odkPath.get(0)+odkPath.get(odkPath.size()-1);

        	FragmentedSubmissionTable table = new FragmentedSubmissionTable(getServerURL(req), odkPath.get(0), em, numEntriesToFetch);
        	ResultTable rt = table.generateRepeatResultTableFrag( 
        			kind, 
        			elementKey, elementReference, elementParentKey, websafeCursorString);    
        	
        	resp.setContentType("text/xml; charset=UTF-8");
        	resp.setCharacterEncoding("UTF-8");
        	table.emitXmlWrappedCsv(out, rt);
        } else if( elementParentKey == null && odkPath != null && odkPath.size() <= 2 && numEntriesToFetch > 0) {
        	// top-level form has no parent...
        	// top-level form can be referenced either by just "form-identity" or by "form-identity/top-level-tag"
	    	// reworked from formxmlservlet.java

        	FragmentedSubmissionTable table = new FragmentedSubmissionTable(getServerURL(req), odkPath.get(0), em, numEntriesToFetch);
	    	ResultTable rt = table.generateResultTableFrag(elementReference, websafeCursorString);
	    	
        	resp.setContentType("text/xml; charset=UTF-8");
        	resp.setCharacterEncoding("UTF-8");
	    	table.emitXmlWrappedCsv(out, rt);
        } else {
            beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
            String requestPath = HtmlUtil.createUrl(getServerURL(req)) + ADDR;
            out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H3, "Parameters are not correctly specified."));
            out.write(HtmlConsts.TABLE_OPEN);
            out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_ROW,
            					HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,"Parameter")
              					+ HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,"Description")
            			)
            		   + HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_ROW,
             				    HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,ServletConsts.ODK_ID)
             				    + HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,"Required for accessing all data associated with a form.  This is a path rooted at the Form Identity displayed in the forms list.")
             		   )
             		   + HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_ROW,
            				    HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,ServletConsts.NUM_ENTRIES)
            				    + HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,"Optional.  The number of rows of data to return in a result csv.  If you are having transmission issues, you may need to reduce the number of records you fetch.  The default is 1000.")
            		   )
              		   + HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_ROW,
              				    HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,ServletConsts.CURSOR)
              				    + HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,"Optional.  Required for accessing subsequent blocks of data.  Supplied as the <cursor> value from the previous web request.")
              		   )
            		   );
            out.write(HtmlConsts.TABLE_CLOSE);

            String formIdentity = "widgets";
            out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.P, "To download a csv fragment for the non-repeating elements of a form, append the Form Identifier and the number of entries to fetch to this url, e.g., "));
            out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.PRE, requestPath + "?" + ServletConsts.ODK_ID + "=" + formIdentity + "&" + ServletConsts.NUM_ENTRIES + "=1000" ));

            out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.P, "The " + ServletConsts.ODK_ID + " parameter supports an xpath-like specification of repeat groups within a form (e.g., widgets/widgets/repeat_a) and primary key restrictions on the last or next-to-last element in the path."));
            out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.UL, 
            		HtmlUtil.wrapWithHtmlTags(HtmlConsts.LI,
            				HtmlUtil.wrapWithHtmlTags(HtmlConsts.PRE, requestPath + "?" + ServletConsts.ODK_ID + "=widgets/widgets/repeat_a") 
            				+ " returns all repeat_a rows.") +
            		HtmlUtil.wrapWithHtmlTags(HtmlConsts.LI,
            				HtmlUtil.wrapWithHtmlTags(HtmlConsts.PRE, requestPath + "?" + ServletConsts.ODK_ID + "=widgets/widgets[@key=\"aaaa\"]/repeat_a") 
            				+ " returns all repeat_a rows for the widgets record identified by key \"aaaa\".") +
            		HtmlUtil.wrapWithHtmlTags(HtmlConsts.LI,
            				HtmlUtil.wrapWithHtmlTags(HtmlConsts.PRE, requestPath + "?" + ServletConsts.ODK_ID + "=widgets/widgets/repeat_a[@key=\"bbb\"]")
            				+ " returns the repeat_a row identified by key \"bbb\".")));
            out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.P, "The data returned is a text/xml document as follows:"));
            out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.PRE,
            				"&lt;entries&gt;\n"+
            				"  &lt;cursor&gt;...&lt;/cursor&gt; &lt;!-- only present if additional records may be fetched --&gt;\n"+
            				"  &lt;header&gt;...&lt;/header&gt; &lt;!-- csv -- property names --&gt;\n"+
            				"  &lt;result&gt;...&lt;/result&gt; &lt;!-- csv -- values -- repeats 0 or more times --&gt;\n"+
            				"&lt;/entries&gt;\n"));
            out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.P, "The returned form data includes an additional property (as the right-most column): KEY.  The KEY value is the URL for this item on the Aggregate server."));
            out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.P, "The returned repeated group data within a form includes two additional properties (as the next-to-right-most and right-most columns): PARENT_KEY and KEY.  The PARENT_KEY value is the URL for the parent item of this repeated group on the Aggregate server; the KEY value is the URL for this repeated group item on the Aggregate server."));
            

            resp.setStatus(400);
            finishBasicHtmlResponse(resp);
        }	
    } catch (DeadlineExceededException e) {
	    // TODO: since this is going to be accessed by a java program,
	    // instead of throwing text exceptions, we should set error codes or something
    	
    	// TODO: do something?
    } catch (ODKFormNotFoundException e) {
        odkIdNotFoundError(resp);
    } catch (ODKIncompleteSubmissionData e) {
        errorRetreivingData(resp);
    } catch (ODKParseException e) {
    	errorBadParam(resp);
	} finally {
        em.close();
    }
  }
}
