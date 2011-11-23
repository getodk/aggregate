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

package org.opendatakit.aggregate.parser;

import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xform.parse.IXFormParserFactory;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.opendatakit.aggregate.constants.ParserConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.FormActionStatusTimestamp;
import org.opendatakit.aggregate.constants.common.GeoPointConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDataModel.ElementType;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.exception.ODKFormAlreadyExistsException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData.Reason;
import org.opendatakit.aggregate.exception.ODKParseException;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.FormDefinition;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.SubmissionAssociationTable;
import org.opendatakit.aggregate.form.XFormParameters;
import org.opendatakit.common.datamodel.DynamicBase;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * Parses an XML definition of an XForm based on java rosa types
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormParserForJavaRosa {
	
  static Log log = LogFactory.getLog(FormParserForJavaRosa.class.getName());
  private static final String BASE64_RSA_PUBLIC_KEY = "base64RsaPublicKey";
  private static final String ENCRYPTED_FORM_DEFINITION = "<?xml version=\"1.0\"?>" +
  	"<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:jr=\"http://openrosa.org/javarosa\">" +
  	"<h:head>" +
		"<h:title>Encrypted Form</h:title>" +
		"<model>" +
			"<instance>" +
				"<data id=\"encrypted\" >" +
					"<meta>" +
						"<timeStart/>" +
						"<timeEnd/>" +
						"<instanceID/>" +
					"</meta>" +
               "<base64EncryptedKey/>" +
					"<encryptedXmlFile/>" +
					"<media>" +
						"<file/>" +
					"</media>" +
				"</data>" +
			"</instance>" +
			"<bind nodeset=\"/data/meta/timeStart\" type=\"datetime\"/>" +
			"<bind nodeset=\"/data/meta/timeEnd\" type=\"datetime\"/>" +
			"<bind nodeset=\"/data/meta/instanceID\" type=\"string\"/>" +
         "<bind nodeset=\"/data/base64EncryptedKey\" type=\"string\"/>" +
			"<bind nodeset=\"/data/encryptedXmlFile\" type=\"binary\"/>" +
			"<bind nodeset=\"/data/media/file\" type=\"binary\"/>" +
		"</model>" +
	"</h:head>" +
	"<h:body>" +
		"<input ref=\"meta/timeStart\"><label>start</label></input>" +
		"<input ref=\"meta/timeEnd\"><label>end</label></input>" +
		"<input ref=\"meta/instanceID\"><label>InstanceID</label></input>" +
      "<input ref=\"base64EncryptedKey\"><label>Encrypted SymmetricKey</label></input>" +
		"<upload ref=\"encryptedXmlFile\" mediatype=\"image/*\"><label>submission</label></upload>" +
		"<repeat nodeset=\"/data/media\">" +
			"<upload ref=\"file\" mediatype=\"image/*\"><label>media file</label></upload>" +
		"</repeat>" +
	"</h:body>" +
	"</h:html>";
  
  private static class XFormParserWithBindEnhancementsFactory implements IXFormParserFactory {

    FormParserForJavaRosa parser;
    
    XFormParserWithBindEnhancementsFactory(FormParserForJavaRosa parser) {
      this.parser = parser;
    }
    
    @Override
    public XFormParser getXFormParser(Reader form) {
      return new XFormParserWithBindEnhancements(parser, form);
    }

    @Override
    public XFormParser getXFormParser(Document form) {
      return new XFormParserWithBindEnhancements(parser, form);
    }

    @Override
    public XFormParser getXFormParser(Reader form, Reader instance) {
      return new XFormParserWithBindEnhancements(parser, form, instance);
    }

    @Override
    public XFormParser getXFormParser(Document form, Document instance) {
      return new XFormParserWithBindEnhancements(parser, form, instance);
    }
  }
  
  private static class XFormParserWithBindEnhancements extends XFormParser {
    private FormParserForJavaRosa parser;
    
    public XFormParserWithBindEnhancements(FormParserForJavaRosa parser, Reader form) {
      super(form);
      this.parser = parser;
    }

    public XFormParserWithBindEnhancements(FormParserForJavaRosa parser, Document form) {
      super(form);
      this.parser = parser;
    }
    
    public XFormParserWithBindEnhancements(FormParserForJavaRosa parser, Reader form, Reader instance) {
      super(form, instance);
      this.parser = parser;
    }
    
    public XFormParserWithBindEnhancements(FormParserForJavaRosa parser, Document form, Document instance) {
      super(form, instance);
      this.parser = parser;
    }

    protected void parseBind(Element e) {
      Vector usedAtts = new Vector();

      DataBinding binding = processStandardBindAttributes( usedAtts, e);

      String value = e.getAttributeValue(ParserConsts.NAMESPACE_ODK, "length");
      if ( value != null ) {
         e.setAttribute(ParserConsts.NAMESPACE_ODK, "length", null);
      }
      
      log.info("Calling handle found value " + ((value == null) ? "null" : value));

      if ( value != null ) {
         Integer iValue = Integer.valueOf(value);
         parser.setNodesetStringLength(e.getAttributeValue(null, "nodeset"), iValue);
      }
      
      //print unused attribute warning message for parent element
      if(XFormUtils.showUnusedAttributeWarning(e, usedAtts)){
         System.out.println(XFormUtils.unusedAttWarning(e, usedAtts));
      }

      addBinding(binding);
    }
  }

  private static synchronized final FormDef parseFormDefinition(String xml, FormParserForJavaRosa parser) throws ODKIncompleteSubmissionData {

    IXFormParserFactory oldFactory = XFormUtils.setXFormParserFactory(new XFormParserWithBindEnhancementsFactory(parser));
	    
	    FormDef formDef = null;
	    try {
	      formDef = XFormUtils.getFormFromInputStream(new ByteArrayInputStream(xml.getBytes()));
	    } catch (Exception e) {
	      throw new ODKIncompleteSubmissionData(e, Reason.BAD_JR_PARSE);
	    } finally {
	      XFormUtils.setXFormParserFactory(oldFactory);
	    }
	    
	    return formDef;
  }
  
  /**
   * The ODK Id that uniquely identifies the form
   */
  private final XFormParameters rootElementDefn;
  private final TreeElement submissionElement;
  private final XFormParameters submissionElementDefn;
  
  private String fdmSubmissionUri;
  private int elementCount = 0;
  private int phantomCount = 0;

  /**
   * The XForm definition in XML
   */
  private final String xml;
  private final Map<String,Integer> stringLengths = new HashMap<String,Integer>();
  private final Map<FormDataModel,Integer> fieldLengths = new HashMap<FormDataModel,Integer>();
  
  private void setNodesetStringLength(String nodeset, Integer length) {
	  stringLengths.put(nodeset, length);
  }
  
  private Integer getNodesetStringLength(TreeElement e) {
	  List<String> path = new ArrayList<String>();
	  while ( e != null && e.getName() != null ) {
		  path.add(e.getName());
		  e = e.getParent();
	  }
	  Collections.reverse(path);
	  
      StringBuilder b = new StringBuilder();
	  for ( String s : path ) {
		b.append("/");
		b.append(s);
	  }
	
	  String nodeset = b.toString();
	  Integer len = stringLengths.get(nodeset);
	  return len;
  }
  
  /**
   * Extract the form id, version and uiVersion.
   * 
   * @param rootElement - the tree element that is the root submission.
   * @param defaultFormIdValue - used if no "id" attribute found.  This should already be slash-substituted.
   * @return
   */
  private XFormParameters extractFormParameters( TreeElement rootElement, String defaultFormIdValue ) {

	String formIdValue = null;
	String versionString = rootElement.getAttributeValue(null, "version");
	String uiVersionString = rootElement.getAttributeValue(null, "uiVersion");
	
	// search for the "id" attribute
	for (int i = 0; i < rootElement.getAttributeCount(); i++) {
	  String name = rootElement.getAttributeName(i);
	  if (name.equals(ParserConsts.FORM_ID_ATTRIBUTE_NAME)) {
	    formIdValue = rootElement.getAttributeValue(i);
		formIdValue = formIdValue.replaceAll(ParserConsts.FORWARD_SLASH, ParserConsts.FORWARD_SLASH_SUBSTITUTION);
	    break;
	  }
	}
	
	return new XFormParameters((formIdValue == null) ? defaultFormIdValue : formIdValue, 
					(versionString == null) ? null : Long.valueOf(versionString),
					(uiVersionString == null) ? null : Long.valueOf(uiVersionString));
  }

  /**
   * Constructor that parses and xform from the input stream supplied and
   * creates the proper ODK Aggregate Form definition.
   * 
   * @param formName
   * @param formXmlData
   * @param inputXml
   * @param fileName
   * @param uploadedFormItems
   * @param warnings -- the builder that will hold all the non-fatal form-creation warnings
   * @param cc
   * @throws ODKFormAlreadyExistsException
   * @throws ODKIncompleteSubmissionData
   * @throws ODKConversionException
   * @throws ODKDatastoreException
   * @throws ODKParseException
   */
  public FormParserForJavaRosa(String formName, MultiPartFormItem formXmlData, String inputXml, String fileName,
	  MultiPartFormData uploadedFormItems, StringBuilder warnings,
      CallingContext cc) throws ODKFormAlreadyExistsException,
      ODKIncompleteSubmissionData, ODKConversionException, ODKDatastoreException,
      ODKParseException {

    if (inputXml == null || formXmlData == null) {
      throw new ODKIncompleteSubmissionData(Reason.MISSING_XML);
    }

    xml = inputXml;
    FormDef formDef = parseFormDefinition(xml, this);

    if (formDef == null) {
        throw new ODKIncompleteSubmissionData("Javarosa failed to construct a FormDef.  Is this an XForm definition?", Reason.BAD_JR_PARSE);
    }
    FormInstance dataModel = formDef.getInstance();
    if ( dataModel == null ) {
    	throw new ODKIncompleteSubmissionData("Javarosa failed to construct a FormInstance.  Is this an XForm definition?", Reason.BAD_JR_PARSE);
    }
    TreeElement rootElement = dataModel.getRoot();

    boolean schemaMalformed = false;
    String schemaValue = dataModel.schema;
    if ( schemaValue != null ) {
	  int idx = schemaValue.indexOf(":");
	  if ( idx != -1 ) {
		  if ( schemaValue.indexOf("/") < idx ) {
			  // malformed...
			  schemaValue = null;
			  schemaMalformed = true;
		  } else {
			  // need to escape all slashes... for xpath processing...
			  schemaValue = schemaValue.replaceAll(ParserConsts.FORWARD_SLASH, ParserConsts.FORWARD_SLASH_SUBSTITUTION);
		  }
	  } else {
		  // malformed...
		  schemaValue = null;
		  schemaMalformed = true;
	  }
    }
    try {
    	rootElementDefn = extractFormParameters( rootElement, schemaValue );
    } catch ( IllegalArgumentException e ) {
    	if ( schemaMalformed ) {
    		throw new ODKIncompleteSubmissionData(
	            "xmlns attribute for the data model is not well-formed: '"
	                + dataModel.schema
	                + "' should be of the form xmlns=\"http://your.domain.org/formId\"\nConsider defining the formId using the 'id' attribute instead of the 'xmlns' attribute (id=\"formId\")",
	                Reason.ID_MALFORMED);
    	} else {
    		throw new ODKIncompleteSubmissionData(
	            "The data model does not have an id or xmlns attribute.  Add an id=\"your.domain.org:formId\" attribute to the top-level instance data element of your form.",
	            Reason.ID_MISSING);
    	}
    }
    
    // obtain form title either from the xform itself or from user entry
    String title = formDef.getTitle();
    if (title == null) {
      if (formName == null) {
        throw new ODKIncompleteSubmissionData(Reason.TITLE_MISSING);
      } else {
        title = formName;
      }
    }
    // clean illegal characters from title
    title = title.replace(BasicConsts.FORWARDSLASH, BasicConsts.EMPTY_STRING);

    TreeElement trueSubmissionElement;
    boolean isEncryptedForm = false;
    boolean isNotUploadableForm = false;
    // Determine the information about the submission...
    SubmissionProfile p = formDef.getSubmissionProfile();
    if ( p == null || p.getRef() == null ) {
    	trueSubmissionElement = rootElement;
    	submissionElementDefn = rootElementDefn;
    } else {
    	trueSubmissionElement = formDef.getInstance().resolveReference(p.getRef());
    	if ( trueSubmissionElement == null ) {
    	  trueSubmissionElement = rootElement;
    	  submissionElementDefn = rootElementDefn;
    	} else {
        try {
          submissionElementDefn = extractFormParameters(trueSubmissionElement, null);
        } catch (Exception e) {
          throw new ODKIncompleteSubmissionData(
              "The non-root submission element in the data model does not have an id attribute.  Add an id=\"your.domain.org:formId\" attribute to the submission element of your form.",
              Reason.ID_MISSING);
        }
    	}
    }

    if ( p != null ) {
        String altUrl = p.getAction();
        isNotUploadableForm = ( altUrl == null || !altUrl.startsWith("http") 
        		|| p.getMethod() == null || !p.getMethod().equals("form-data-post"));
    }

    if ( isNotUploadableForm ) {
    	log.info("Form " + submissionElementDefn.formId + 
    	" is not uploadable (submission method is not form-data-post or does not have an http: or https: url. ");
    }
    
    // insist that the submission element and root element have the same 
    // formId, modelVersion and uiVersion.
    if ( !submissionElementDefn.equals(rootElementDefn) ) {
    	throw new ODKIncompleteSubmissionData("submission element and root element differ in their values for: formId, version or uiVersion.", Reason.MISMATCHED_SUBMISSION_ELEMENT);
    }
    
    String publicKey = null;
    if ( p != null ) {
    	publicKey = p.getAttribute(BASE64_RSA_PUBLIC_KEY);
    }
    
    // now see if we are encrypted -- if so, fake the submission element to be 
    // the parsing of the ENCRYPTED_FORM_DEFINITION
    if ( publicKey == null || publicKey.length() == 0 ) {
    	// not encrypted...
    	submissionElement = trueSubmissionElement;
    } else {
    	isEncryptedForm = true;
    	// encrypted -- use the encrypted form template (above) to define the storage for this form.
        formDef = parseFormDefinition(ENCRYPTED_FORM_DEFINITION, this);

        if (formDef == null) {
            throw new ODKIncompleteSubmissionData("Javarosa failed to construct Encrypted FormDef!", Reason.BAD_JR_PARSE);
        }
        dataModel = formDef.getInstance();
        if ( dataModel == null ) {
        	throw new ODKIncompleteSubmissionData("Javarosa failed to construct Encrypted FormInstance!", Reason.BAD_JR_PARSE);
        }
        submissionElement = dataModel.getRoot();
    }

    // Construct the base table prefix candidate from the submissionElementDefn.formId.
    String persistenceStoreFormId = submissionElementDefn.formId;
    if ( persistenceStoreFormId.indexOf(':') != -1 ) {
    	// this is likely an xmlns-style URI (http://..../)
    	// remove the scheme://domain.org/ from this name, as it is likely
    	// to be common across all forms. Use the remainder as the base table
    	// prefix candidate.
        persistenceStoreFormId = submissionElementDefn.formId.substring(submissionElementDefn.formId.indexOf(':') + 1);
        int idxSlashAfterDomain = persistenceStoreFormId.indexOf('/',2);
        if ( idxSlashAfterDomain != -1) {
        	// remove the domain from the xmlns -- we'll use the string after the domain for the tablespace.
            persistenceStoreFormId = persistenceStoreFormId.substring(idxSlashAfterDomain+1);
        }
    }
    // First, replace all slash substitutions with underscores.
    // Then replace all non-alphanumerics with underscores.
    // Then trim any leading underscores.
    persistenceStoreFormId = persistenceStoreFormId.replace(ParserConsts.FORWARD_SLASH_SUBSTITUTION, "_");
    persistenceStoreFormId = persistenceStoreFormId.replaceAll("[^\\p{Digit}\\p{Lu}\\p{Ll}\\p{Lo}]", "_");
    persistenceStoreFormId = persistenceStoreFormId.replaceAll("^_*","");

    initHelper(uploadedFormItems, formXmlData, inputXml,
    		  title, persistenceStoreFormId, isEncryptedForm, formDef, warnings, cc);
  }
  
  enum AuxType { NONE, BC_REF, REF_BLOB, GEO_LAT, GEO_LNG, GEO_ALT, GEO_ACC, LONG_STRING_REF, REF_TEXT };
  
  private String generatePhantomKey( String uriSubmissionFormModel ) {
	  return String.format("elem+%1$s(%2$08d-phantom:%3$08d)", uriSubmissionFormModel,
				  		elementCount, ++phantomCount );
  }
  
  private void setPrimaryKey( FormDataModel m, String uriSubmissionFormModel, AuxType aux ) {
	  String pkString;
	  if ( aux != AuxType.NONE ) {
		  pkString = String.format("elem+%1$s(%2$08d-%3$s)", uriSubmissionFormModel,
				  		elementCount, aux.toString().toLowerCase());
	  } else {
		  ++elementCount;
		  pkString = String.format("elem+%1$s(%2$08d)", uriSubmissionFormModel, elementCount);
	  }
	  m.setStringField(m.primaryKey, pkString);
  }
  
  private void initHelper(MultiPartFormData uploadedFormItems, MultiPartFormItem xformXmlData,  
		  String inputXml, String title, String persistenceStoreFormId, boolean isEncryptedForm, 
		  FormDef formDef, StringBuilder warnings, CallingContext cc) throws ODKDatastoreException, ODKFormAlreadyExistsException, ODKParseException, ODKConversionException {
    
    // we can't create or update a FormInfo record if there is a pending deletion for this same id.
	FormActionStatusTimestamp formDeletionStatus;
	formDeletionStatus = MiscTasks.getFormDeletionStatusTimestampOfFormId(rootElementDefn.formId, cc);
	if ( formDeletionStatus != null ) {
		throw new ODKFormAlreadyExistsException("This form and its data have not yet been fully deleted from the server. Please wait a few minutes and retry.");
    }
	if ( !submissionElementDefn.formId.equals(rootElementDefn.formId)) {
		formDeletionStatus = MiscTasks.getFormDeletionStatusTimestampOfFormId(submissionElementDefn.formId, cc);
		if ( formDeletionStatus != null ) {
			throw new ODKFormAlreadyExistsException("This form and its data have not yet been fully deleted from the server. Please wait a few minutes and retry.");
		}
	}
    
    /////////////////
    // Step 1: create or fetch the Form (FormInfo) submission
    //
    // This allows us to delete the form if upload goes bad...
	// TODO: the following function throws an exception unless new or identical inputXml
    byte[] xmlBytes;
    try {
		xmlBytes = inputXml.getBytes(HtmlConsts.UTF8_ENCODE);
	} catch (UnsupportedEncodingException e) {
		throw new IllegalStateException("not reachable");
	}
	
	// form downloads are immediately enabled unless the upload specifies that they shouldn't be. 
    String isIncompleteFlag = uploadedFormItems.getSimpleFormField(ServletConsts.TRANSFER_IS_INCOMPLETE);
    boolean isDownloadEnabled = ( isIncompleteFlag == null || isIncompleteFlag.trim().length() == 0 );

    IForm formInfo = FormFactory.createOrFetchFormId(rootElementDefn, isEncryptedForm,
    					title, xmlBytes, isDownloadEnabled, cc);
    boolean newlyCreatedXForm = formInfo.isNewlyCreated();

    Set<Map.Entry<String,MultiPartFormItem>> fileSet = uploadedFormItems.getFileNameEntrySet();
    for ( Map.Entry<String,MultiPartFormItem> itm : fileSet) {
    	if ( itm.getValue() == xformXmlData ) continue;// ignore the xform -- stored above.
    	formInfo.setXFormMediaFile(
    			rootElementDefn.modelVersion, rootElementDefn.uiVersion,
				itm.getValue(), cc);
    }
    // Determine the information about the submission...
	formInfo.setIsComplete(true);
    formInfo.persist(cc);

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    
    FormDefinition fdDefined = null;
    try {
    	fdDefined = FormDefinition.getFormDefinition(submissionElementDefn, cc);
    } catch ( IllegalStateException e ) {
    	e.printStackTrace();
    	throw new ODKFormAlreadyExistsException("Internal error: the form already exists but has a bad form definition.  Delete it.");
    }
    if ( fdDefined != null ) {
        // get most recent form-deletion statuses
    	if ( newlyCreatedXForm ) {
    		throw new ODKFormAlreadyExistsException("Internal error: Completely new file has pre-existing form definition");
    	}
    	return;
    }

    // we don't have an existing form definition
    // -- create a submission association table entry mapping to what will be the model.
    // -- then create the model and iterate on manifesting it in the database.
    SubmissionAssociationTable sa = 
    	SubmissionAssociationTable.assertSubmissionAssociation(formInfo.getKey().getKey(), submissionElementDefn, cc);
    fdmSubmissionUri = sa.getUriSubmissionDataModel();
    
    // so we have the formInfo record, but no data model backing it.
    // Find the submission associated with this form...
    
    final List<FormDataModel> fdmList = new ArrayList<FormDataModel>();

    final Set<CommonFieldsBase> createdRelations = new HashSet<CommonFieldsBase>();
    
    try {
	    //////////////////////////////////////////////////
	    // Step 2: Now build up the parse tree for the form...
	    //
	    final FormDataModel fdm = FormDataModel.assertRelation(cc);
	    
	    // we haven't actually constructed the fdm record yet, so use the 
	    // relation when creating the entity key...
	    final EntityKey k = new EntityKey( fdm, fdmSubmissionUri);
	
	    NamingSet opaque = new NamingSet();
	
	    // construct the data model with table and column placeholders.
	    // assumes that the root is a non-repeating group element.
	    final String tableNamePlaceholder = opaque
	        .getTableName(fdm.getSchemaName(), persistenceStoreFormId, "", "CORE");
	
	    constructDataModel(opaque, k, fdmList, fdm, k.getKey(), 1, persistenceStoreFormId, "",
	        tableNamePlaceholder, submissionElement, warnings, cc);
	
	    // find a good set of names...
	    // this also ensures that the table names don't overlap existing tables
	    // in the datastore.
	    opaque.resolveNames(ds, user);
	    
	    // debug output
	    // for ( FormDataModel m : fdmList ) {
	    // 	m.print(System.out);
	    // }
	
	    // and revise the data model with those names...
	    for (FormDataModel m : fdmList) {
	      String tablePlaceholder = m.getPersistAsTable();
	      if (tablePlaceholder == null)
	        continue;
	
	      String columnPlaceholder = m.getPersistAsColumn();
	
	      String tableName = opaque.resolveTablePlaceholder(tablePlaceholder);
	      String columnName = opaque.resolveColumnPlaceholder(tablePlaceholder, columnPlaceholder);
	      
	      m.setPersistAsColumn(columnName);
	      m.setPersistAsTable(tableName);
	    }
	
	    /////////////////////////////////////////////
	    // Step 3: create the backing tables...
	    //
	    // OK. At this point, the construction gets a bit ugly.
	    // We need to handle the possibility that the table
	    // needs to be split into phantom tables.
	    // That happens if the table exceeds the maximum row
	    // size for the persistence layer.
	
	    // we do this by constructing the form definition from the fdmList
	    // and then testing for successful creation of each table it defines.
	    // If that table cannot be created, we subdivide it, rearranging
	    // the structure of the fdmList. Repeat until no errors.
	    // Very error prone!!!
	    // 
	    try {
		    for (;;) {
		      FormDefinition fd = new FormDefinition(sa, submissionElementDefn, fdmList, cc);
		      
		      List<CommonFieldsBase> badTables = new ArrayList<CommonFieldsBase>();
		
		      for (CommonFieldsBase tbl : fd.getBackingTableSet()) {
		
		        try {
		        	// patch up tbl with desired lengths of string fields...
		        	for ( FormDataModel m : fdmList ) {
		        		if ( m.getElementType().equals(ElementType.STRING) ) {
		        			DataField f = m.getBackingKey();
		        			Integer i = fieldLengths.get(m);
		        			if ( f != null && i != null ) {
		        				f.setMaxCharLen(new Long(i));
		        			}
		        		}
		        	}
		        	ds.assertRelation(tbl, user);
		        	createdRelations.add(tbl);
		        } catch (Exception e1) {
		          // assume it is because the table is too wide...
		          log.warn("Create failed -- assuming phantom table required " + tbl.getSchemaName() + "."
		                  + tbl.getTableName());
		          try {
		            ds.dropRelation(tbl, user);
		          } catch (Exception e2) {
		            // no-op
		          }
		          if ((tbl instanceof DynamicBase) ||
		        	  (tbl instanceof TopLevelDynamicBase)) {
			          badTables.add(tbl); // we know how to subdivide these
		          } else {
		        	  throw e1; // must be something amiss with database...
		          }
		        }
		      }
		
		      for (CommonFieldsBase tbl : badTables) {
		        // dang. We need to create phantom tables...
		        orderlyDivideTable(fdmList, FormDataModel.assertRelation(cc), 
		        		tbl, opaque, cc);
		      }
		
		      if (badTables.isEmpty())
		        break;
		      
		      // reset the derived fields so that the FormDefinition construction will work.
		      for ( FormDataModel m : fdmList ) {
		    	  m.resetDerivedFields();
		      }
		    }
	    } catch ( Exception e ) {
		      FormDefinition fd = new FormDefinition(sa, submissionElementDefn, fdmList, cc);
		  	
		      for (CommonFieldsBase tbl : fd.getBackingTableSet()) {
		    	  try {
		    		  ds.dropRelation(tbl, user);
		    		  createdRelations.remove(tbl);
		    	  } catch ( Exception e3 ) {
		    		  // do nothing...
		    		  e3.printStackTrace();
		    	  }
		      }
		      if ( !createdRelations.isEmpty()) {
		    	  log.error("createdRelations not fully unwound!");
		    	  for (CommonFieldsBase tbl : createdRelations ) {
			    	  try {
			    		  log.error("--dropping " + tbl.getSchemaName() + "." + tbl.getTableName());
			    		  ds.dropRelation(tbl, user);
			    		  createdRelations.remove(tbl);
			    	  } catch ( Exception e3 ) {
			    		  // do nothing...
			    		  e3.printStackTrace();
			    	  }
		    	  }
		    	  createdRelations.clear();
		      }
	    }
    
    // TODO: if the above gets killed, how do we clean up?
    } catch ( ODKParseException e ) {
    	formInfo.deleteForm(cc);
    	throw e;
    } catch ( ODKDatastoreException e ) {
    	formInfo.deleteForm(cc);
    	throw e;
    }

    //////////////////////////////////////////////
    // Step 4: record the data model...
    //
    // if we get here, we were able to create the tables -- record the
    // form description....
	ds.putEntities(fdmList, user);
	
    // TODO: if above write fails, how do we clean this up?
	
	// and update the complete flag to indicate that upload was fully successful.
	sa.setIsPersistenceModelComplete(true);
	ds.putEntity(sa, user);
  }

  /**
   * The creation of the tbl relation has failed.  
   * We need to split it into multiple sub-tables and try again.
   * 
   * @param fdmList
   * @param fdmRelation
   * @param tbl
   * @param newPhantomTableName
   */
  private void orderlyDivideTable(List<FormDataModel> fdmList, FormDataModel fdmRelation,
      CommonFieldsBase tbl, NamingSet opaque, CallingContext cc) {
	  
    // Find out how many columns it has...
	int nCol = tbl.getFieldList().size();
	if ( tbl instanceof TopLevelDynamicBase ) {
		nCol = nCol - TopLevelDynamicBase.ADDITIONAL_COLUMN_COUNT - 1;
	} else if ( tbl instanceof DynamicBase ) {
		nCol = nCol - DynamicBase.ADDITIONAL_COLUMN_COUNT - 1;
	}

    if (nCol < 2) {
      throw new IllegalStateException("Unable to subdivide instance table! " + tbl.getSchemaName()
          + "." + tbl.getTableName());
    }

    // search the fdmList for the most-enclosing element that uses this tbl as its backingObject.
    FormDataModel parentTable = null;

    // Step 1: find any FormDataModel that uses tbl as its backing object.
    for (FormDataModel m : fdmList) {
      if (tbl.equals(m.getBackingObjectPrototype())) {
        parentTable = m; // anything we find is good enough...
        break;
      }
    }
    // we should have found something...
    if (parentTable == null) {
      throw new IllegalStateException("Unable to locate model for backing table");
    }

    // Step 2: chain up to the parent whose parent doesn't have tbl as its backing object
    while (parentTable.getParent() != null) {
      FormDataModel parent = parentTable.getParent();
      if (!tbl.equals(parent.getBackingObjectPrototype()))
        break;
      // daisy-chain up to parent
      // we must have had an element or a subordinate group...
      parentTable = parent;
    }

    // go through the parent's children identifying those that 
    // are backed by the table we need to split.
    List<FormDataModel> topElementChange = new ArrayList<FormDataModel>();
    List<FormDataModel> groups = new ArrayList<FormDataModel>();
    for (;;) {
      for (FormDataModel m : parentTable.getChildren()) {
    	// ignore the choice and binary data fields of the parentTable
        if (tbl.equals(m.getBackingObjectPrototype())) {
          // geopoints, phantoms and groups don't have backing keys
          if (m.getBackingKey() != null ) {
            topElementChange.add(m);
          } else {
            int count = recursivelyCountChildrenInSameTable(m);
            if ( (nCol < 4*count) || (count > 10) ) {
            	// it is big enough to consider moving...
                groups.add(m);
            } else {
            	// clump it into the individual elements to move...
            	topElementChange.add(m);
            }
          }
        }
      }
      if (groups.size() + topElementChange.size() == 1) {
        // we have a bogus parent element -- it has only one group
        // in it -- recurse down the groups until we get something
        // with multiple elements. If it is just a single field,
        // we have big problems...
        parentTable = groups.get(0);
        groups.clear();
        topElementChange.clear();

        if (parentTable == null) {
          throw new IllegalStateException(
              "Are there database problems? Failure in create table when there are no nested groups!");
        }
        // note that we don't have to patch up the parentTable we are
        // moving off of, because the tbl will continue to exist.  We
        // just need to move some of its contents to a second table, 
        // either by moving a nested group or geopoint off, or by 
        // creating a phantom table.
      } else {
        // OK we have a chance to do something at this level...
        break;
      }
    }

    // If we have any decent-sized groups, we should cleave off up to 2/3 of the 
    // total elements that may be under a group...
    if (groups.size() > 0) {
      // order the list from high to low...
      Collections.sort(groups, new Comparator<FormDataModel>() {
		@Override
		public int compare(FormDataModel o1, FormDataModel o2) {
	        int c1 = recursivelyCountChildrenInSameTable(o1);
	        int c2 = recursivelyCountChildrenInSameTable(o2);
	        if ( c1 > c2 ) return -1;
	        if ( c1 < c2 ) return 1;
	        return 0;
		}
      });

      // go through the list moving the larger groups into tables
      // until close to half of the elements are moved...
      int cleaveCount = 0;
      for ( FormDataModel m : groups ) {
    	  int groupSize = recursivelyCountChildrenInSameTable(m);
    	  if ( cleaveCount+groupSize > (3*nCol)/4) {
    		  continue; // just too big to split this way see if there is a smaller group...
    	  }
          String newGroupTable;
          try {
            newGroupTable = opaque.generateUniqueTableName(tbl.getSchemaName(), tbl.getTableName(),
            	  				cc);
          } catch (ODKDatastoreException e) {
            e.printStackTrace();
            throw new IllegalStateException("unable to interrogate database");
          }
          recursivelyReassignChildren(m, tbl, newGroupTable);
          cleaveCount += groupSize;
          // and if we have cleaved over half, (divide and conquer), retry it with the database.
          if ( cleaveCount > (nCol/2) ) return;
      }
      // and otherwise, if we did cleave anything off, try anyway...
      // the next time through, we won't have any groups and will need
      // to create phantom tables, so it is worth trying for this here now...
      if ( cleaveCount > 0 ) return;
    }

    // Urgh! we don't have a nested group we can cleave off.
    // or the nested groups are all small ones.  Create a 
    // phantom table.  We need to preserve the parent-child
    // relationship and the ordinal ordering even for the 
    // external tables like choices and binary objects.
    //
    // The children array is ordered by ordinal number, 
    // so we just need to get that, and update the entries
    // in the last half of the array.
    String phantomURI = generatePhantomKey(fdmSubmissionUri);
    String newPhantomTableName;
    try {
      newPhantomTableName = opaque.generateUniqueTableName(tbl.getSchemaName(), tbl.getTableName(),
      		cc);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw new IllegalStateException("unable to interrogate database");
    }
    int desiredOriginalTableColCount = (nCol / 2);
    List<FormDataModel> children = parentTable.getChildren();
    int skipCleaveCount = 0;
    int idxStart;
    for (idxStart = 0 ; idxStart < children.size() ; ++idxStart ) {
    	FormDataModel m = children.get(idxStart);
        if (!tbl.equals(m.getBackingObjectPrototype())) continue;        
    	if (m.getBackingKey() == null) {
    		skipCleaveCount += recursivelyCountChildrenInSameTable(m);
    	} else {
    		++skipCleaveCount;
    	}
    	if ( skipCleaveCount > desiredOriginalTableColCount ) break;
    }
    // everything after idxStart should be moved to be "under" the 
    // phantom table.
    FormDataModel firstToMove = children.get(++idxStart);
    // data record...
    FormDataModel d = cc.getDatastore().createEntityUsingRelation(fdmRelation, cc.getCurrentUser());
    fdmList.add(d);
    d.setStringField(fdmRelation.primaryKey, phantomURI);
    d.setOrdinalNumber(firstToMove.getOrdinalNumber());
    d.setUriSubmissionDataModel(fdmSubmissionUri);
    d.setParentUriFormDataModel(parentTable.getUri());
    d.setElementName(null);
    d.setElementType(FormDataModel.ElementType.PHANTOM);
    d.setPersistAsColumn(null);
    d.setPersistAsTable(newPhantomTableName);
    d.setPersistAsSchema(fdmRelation.getSchemaName());

    // OK -- update ordinals and move remaining columns...
    long ordinalNumber = 0L;
    for ( ; idxStart < children.size() ; ++ idxStart ) {
    	FormDataModel m = children.get(idxStart);
        m.setParentUriFormDataModel(phantomURI);
        m.setOrdinalNumber(++ordinalNumber);
    	recursivelyReassignChildren(m, tbl, newPhantomTableName);
    }
  }

  private int recursivelyCountChildrenInSameTable(FormDataModel parent) {

    int count = 0;
    for (FormDataModel m : parent.getChildren()) {
      if (parent.getPersistAsTable().equals(m.getPersistAsTable())
          && parent.getPersistAsSchema().equals(m.getPersistAsSchema())) {
        count += recursivelyCountChildrenInSameTable(m);
      }
    }
    if (parent.getPersistAsColumn() != null) {
      count++;
    }
    return count;
  }

  private void recursivelyReassignChildren(FormDataModel biggest, CommonFieldsBase tbl, String newPhantomTableName) {
	  
    if (!tbl.equals(biggest.getBackingObjectPrototype())) return;

    biggest.setPersistAsTable(newPhantomTableName);

	for (FormDataModel m : biggest.getChildren()) {
		recursivelyReassignChildren(m, tbl, newPhantomTableName);
    }

  }

  @SuppressWarnings("unused")
  private void printTreeElementInfo(TreeElement treeElement) {
	    System.out.println("processing te: " + treeElement.getName() + " type: " + treeElement.dataType
	            + " repeatable: " + treeElement.repeatable);
  }
  
  /**
   * Used to recursively process the xform definition tree to create the form
   * data model.
   * 
   * @param treeElement
   *          java rosa tree element
   * 
   * @param parentKey
   *          key from the parent form for proper entity group usage in gae
   * 
   * @param parent
   *          parent form element
   * 
   * @throws ODKEntityPersistException
   * @throws ODKParseException
   * 
   */

  private void constructDataModel(final NamingSet opaque, final EntityKey k,
      final List<FormDataModel> dmList, final FormDataModel fdm, 
      String parent, int ordinal, String tablePrefix, String nrGroupPrefix, String tableName,
      TreeElement treeElement, StringBuilder warnings, CallingContext cc) throws ODKEntityPersistException, ODKParseException {

	// for debugging: printTreeElementInfo(treeElement);

    FormDataModel d;

    FormDataModel.ElementType et;
    String persistAsTable = tableName;
    String originalPersistAsColumn = opaque.getColumnName(persistAsTable, nrGroupPrefix,
        treeElement.getName());
    String persistAsColumn = originalPersistAsColumn;

    switch (treeElement.dataType) {
    case org.javarosa.core.model.Constants.DATATYPE_TEXT:/**
       * Text question type.
       */
      et = FormDataModel.ElementType.STRING;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_INTEGER:/**
       * Numeric question
       * type. These are numbers without decimal points
       */
      et = FormDataModel.ElementType.INTEGER;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_DECIMAL:/**
       * Decimal question
       * type. These are numbers with decimals
       */
      et = FormDataModel.ElementType.DECIMAL;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_DATE:/**
       * Date question type.
       * This has only date component without time.
       */
      et = FormDataModel.ElementType.JRDATE;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_TIME:/**
       * Time question type.
       * This has only time element without date
       */
      et = FormDataModel.ElementType.JRTIME;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_DATE_TIME:/**
       * Date and Time
       * question type. This has both the date and time components
       */
      et = FormDataModel.ElementType.JRDATETIME;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_CHOICE:/**
       * This is a question
       * with alist of options where not more than one option can be selected at
       * a time.
       */
      et = FormDataModel.ElementType.STRING;
      // et = FormDataModel.ElementType.SELECT1;
      // persistAsColumn = null;
      // persistAsTable = opaque.getTableName(fdm.getSchemaName(),
      // tablePrefix, nrGroupPrefix, treeElement.getName());
      break;
    case org.javarosa.core.model.Constants.DATATYPE_CHOICE_LIST:/**
       * This is a
       * question with alist of options where more than one option can be
       * selected at a time.
       */
      et = FormDataModel.ElementType.SELECTN;
      opaque.removeColumnName(persistAsTable, persistAsColumn);
      persistAsColumn = null;
      persistAsTable = opaque.getTableName(fdm.getSchemaName(), tablePrefix, nrGroupPrefix,
          treeElement.getName());
      break;
    case org.javarosa.core.model.Constants.DATATYPE_BOOLEAN:/**
       * Question with
       * true and false answers.
       */
      et = FormDataModel.ElementType.BOOLEAN;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_GEOPOINT:/**
       * Question with
       * location answer.
       */
      et = FormDataModel.ElementType.GEOPOINT;
      opaque.removeColumnName(persistAsTable, persistAsColumn);
      persistAsColumn = null; // structured field
      break;
    case org.javarosa.core.model.Constants.DATATYPE_BARCODE:/**
       * Question with
       * barcode string answer.
       */
      et = FormDataModel.ElementType.STRING;
      break;
    case org.javarosa.core.model.Constants.DATATYPE_BINARY:/**
       * Question with
       * external binary answer.
       */
      et = FormDataModel.ElementType.BINARY;
      opaque.removeColumnName(persistAsTable, persistAsColumn);
      persistAsColumn = null;
      persistAsTable = opaque.getTableName(fdm.getSchemaName(), tablePrefix, nrGroupPrefix,
          treeElement.getName() + "_BN");
      break;

    case org.javarosa.core.model.Constants.DATATYPE_NULL: /*
                                                           * for nodes that have
                                                           * no data, or data
                                                           * type otherwise
                                                           * unknown
                                                           */
      if (treeElement.repeatable) {
    	// repeatable group...
        opaque.removeColumnName(persistAsTable, persistAsColumn);
        persistAsColumn = null;
        et = FormDataModel.ElementType.REPEAT;
        persistAsTable = opaque.getTableName(fdm.getSchemaName(), tablePrefix, nrGroupPrefix,
            treeElement.getName());
      } else if (treeElement.getNumChildren() == 0 && dmList.size() != 0) {
        // assume fields that don't have children are string fields.
    	// but exclude the top-level group, as somebody might define an empty form.
        // the developer likely has not set a type for the field.
        et = FormDataModel.ElementType.STRING;
        log.warn("Element " + getTreeElementPath(treeElement) + " does not have a type");
        warnings.append("<tr><td>");
        warnings.append(getTreeElementPath(treeElement));
        warnings.append("</td></tr>");
      } else {
    	/* one or more children -- this is a non-repeating group */
        opaque.removeColumnName(persistAsTable, persistAsColumn);
        persistAsColumn = null;
        et = FormDataModel.ElementType.GROUP;
      }
      break;

    default:
    case org.javarosa.core.model.Constants.DATATYPE_UNSUPPORTED:
      et = FormDataModel.ElementType.STRING;
      break;
    }

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    // data record...
    d = ds.createEntityUsingRelation(fdm, user);
    setPrimaryKey( d, fdmSubmissionUri, AuxType.NONE );
    dmList.add(d);
    final String groupURI = d.getUri();
    d.setOrdinalNumber(Long.valueOf(ordinal));
    d.setUriSubmissionDataModel(k.getKey());
    d.setParentUriFormDataModel(parent);
    d.setElementName(treeElement.getName());
    d.setElementType(et);
    d.setPersistAsColumn(persistAsColumn);
    d.setPersistAsTable(persistAsTable);
    d.setPersistAsSchema(fdm.getSchemaName());
    
    if ( et.equals(ElementType.STRING) ) {
    	// track the preferred string lengths of the string fields
    	Integer len = getNodesetStringLength(treeElement);
    	if ( len != null ) {
    		fieldLengths.put(d, len);
    	}
    }

    // and patch up the tree elements that have multiple fields...
    switch (et) {
    case BINARY:
      // binary elements have two additional tables associated with them
      // -- the _REF and _BLB tables (in addition to _BIN above).
      persistAsTable = opaque.getTableName(fdm.getSchemaName(), tablePrefix, nrGroupPrefix,
          treeElement.getName() + "_REF");

      // record for VersionedBinaryContentRefBlob..
      d = ds.createEntityUsingRelation(fdm, user);
	  setPrimaryKey( d, fdmSubmissionUri, AuxType.BC_REF );
	  dmList.add(d);
      final String bcbURI = d.getUri();
      d.setOrdinalNumber(1L);
      d.setUriSubmissionDataModel(k.getKey());
      d.setParentUriFormDataModel(groupURI);
      d.setElementName(treeElement.getName());
      d.setElementType(FormDataModel.ElementType.BINARY_CONTENT_REF_BLOB);
      d.setPersistAsColumn(null);
      d.setPersistAsTable(persistAsTable);
      d.setPersistAsSchema(fdm.getSchemaName());

      persistAsTable = opaque.getTableName(fdm.getSchemaName(), tablePrefix, nrGroupPrefix,
          treeElement.getName() + "_BLB");

      // record for RefBlob...
      d = ds.createEntityUsingRelation(fdm, user);
	  setPrimaryKey( d, fdmSubmissionUri, AuxType.REF_BLOB );
	  dmList.add(d);
      d.setOrdinalNumber(1L);
      d.setUriSubmissionDataModel(k.getKey());
      d.setParentUriFormDataModel(bcbURI);
	  d.setElementName(treeElement.getName());
	  d.setElementType(FormDataModel.ElementType.REF_BLOB);
      d.setPersistAsColumn(null);
      d.setPersistAsTable(persistAsTable);
      d.setPersistAsSchema(fdm.getSchemaName());
      break;

    case GEOPOINT:
      // geopoints are stored as 4 fields (_LAT, _LNG, _ALT, _ACC) in the
      // persistence layer.
      // the geopoint attribute itself has no column, but is a placeholder
      // within
      // the data model for the expansion set of these 4 fields.

      persistAsColumn = opaque.getColumnName(persistAsTable, nrGroupPrefix, treeElement.getName()
          + "_LAT");

      d = ds.createEntityUsingRelation(fdm, user);
	  setPrimaryKey( d, fdmSubmissionUri, AuxType.GEO_LAT );
      dmList.add(d);
      d.setOrdinalNumber(Long.valueOf(GeoPointConsts.GEOPOINT_LATITUDE_ORDINAL_NUMBER));
      d.setUriSubmissionDataModel(k.getKey());
      d.setParentUriFormDataModel(groupURI);
	  d.setElementName(treeElement.getName());
	  d.setElementType(FormDataModel.ElementType.DECIMAL);
      d.setPersistAsColumn(persistAsColumn);
      d.setPersistAsTable(persistAsTable);
      d.setPersistAsSchema(fdm.getSchemaName());

      persistAsColumn = opaque.getColumnName(persistAsTable, nrGroupPrefix, treeElement.getName()
          + "_LNG");

      d = ds.createEntityUsingRelation(fdm, user);
	  setPrimaryKey( d, fdmSubmissionUri, AuxType.GEO_LNG );
      dmList.add(d);
      d.setOrdinalNumber(Long.valueOf(GeoPointConsts.GEOPOINT_LONGITUDE_ORDINAL_NUMBER));
      d.setUriSubmissionDataModel(k.getKey());
      d.setParentUriFormDataModel(groupURI);
	  d.setElementName(treeElement.getName());
	  d.setElementType(FormDataModel.ElementType.DECIMAL);
      d.setPersistAsColumn(persistAsColumn);
      d.setPersistAsTable(persistAsTable);
      d.setPersistAsSchema(fdm.getSchemaName());

      persistAsColumn = opaque.getColumnName(persistAsTable, nrGroupPrefix, treeElement.getName()
          + "_ALT");

      d = ds.createEntityUsingRelation(fdm, user);
	  setPrimaryKey( d, fdmSubmissionUri, AuxType.GEO_ALT );
      dmList.add(d);
      d.setOrdinalNumber(Long.valueOf(GeoPointConsts.GEOPOINT_ALTITUDE_ORDINAL_NUMBER));
      d.setUriSubmissionDataModel(k.getKey());
      d.setParentUriFormDataModel(groupURI);
	  d.setElementName(treeElement.getName());
	  d.setElementType(FormDataModel.ElementType.DECIMAL);
      d.setPersistAsColumn(persistAsColumn);
      d.setPersistAsTable(persistAsTable);
      d.setPersistAsSchema(fdm.getSchemaName());

      persistAsColumn = opaque.getColumnName(persistAsTable, nrGroupPrefix, treeElement.getName()
          + "_ACC");

      d = ds.createEntityUsingRelation(fdm, user);
	  setPrimaryKey( d, fdmSubmissionUri, AuxType.GEO_ACC );
      dmList.add(d);
      d.setOrdinalNumber(Long.valueOf(GeoPointConsts.GEOPOINT_ACCURACY_ORDINAL_NUMBER));
      d.setUriSubmissionDataModel(k.getKey());
      d.setParentUriFormDataModel(groupURI);
	  d.setElementName(treeElement.getName());
      d.setElementType(FormDataModel.ElementType.DECIMAL);
      d.setPersistAsColumn(persistAsColumn);
      d.setPersistAsTable(persistAsTable);
      d.setPersistAsSchema(fdm.getSchemaName());
      break;

    case GROUP:
      // non-repeating group - this modifies the group prefix,
      // and all children are emitted.
      if (!parent.equals(k.getKey())) {
        // incorporate the group name only if it isn't the top-level
        // group.
        if (nrGroupPrefix.length() == 0) {
          nrGroupPrefix = treeElement.getName();
        } else {
          nrGroupPrefix = nrGroupPrefix + "_" + treeElement.getName();
        }
      }
      // OK -- group with at least one element -- assume no value...
      // TreeElement list has the begin and end tags for the nested groups.
      // Swallow the end tag by looking to see if the prior and current
      // field names are the same.
      TreeElement prior = null;
      int trueOrdinal = 0;
      for (int i = 0; i < treeElement.getNumChildren(); ++i) {
    	  TreeElement current = (TreeElement) treeElement.getChildAt(i);
    	  // TODO: make this pay attention to namespace of the tag...
    	  if ( (prior != null) && 
    		   (prior.getName().equals(current.getName())) ) {
    		  // it is the end-group tag... seems to happen with two adjacent repeat groups
    		  log.info("repeating tag at " + i + " skipping " + current.getName());
    		  prior = current;
    	  } else {
    		  constructDataModel(opaque, k, dmList, fdm, groupURI, ++trueOrdinal, tablePrefix,
    				  nrGroupPrefix, persistAsTable, current, warnings, cc);
    		  prior = current;
    	  }
      }
      break;

    case REPEAT:
      // repeating group - clears group prefix
      // and all children are emitted.
      // TreeElement list has the begin and end tags for the nested groups.
      // Swallow the end tag by looking to see if the prior and current
      // field names are the same.
      prior = null;
      trueOrdinal = 0;
      for (int i = 0; i < treeElement.getNumChildren(); ++i) {
    	  TreeElement current = (TreeElement) treeElement.getChildAt(i);
    	  // TODO: make this pay attention to namespace of the tag...
    	  if ( (prior != null) && 
    		   (prior.getName().equals(current.getName())) ) {
    		  // it is the end-group tag... seems to happen with two adjacent repeat groups
    		  log.info("repeating tag at " + i + " skipping " + current.getName());
    		  prior = current;
    	  } else {
    		  constructDataModel(opaque, k, dmList, fdm, groupURI, ++trueOrdinal, tablePrefix,
    				  "", persistAsTable, current, warnings, cc);
    		  prior = current;
    	  }
      }
      break;
    }
  }
  
  public String getTreeElementPath(TreeElement e) {
	  if ( e ==  null ) return null;
	  String s = getTreeElementPath(e.getParent());
	  if ( s == null ) return e.getName();
	  return s + "/" + e.getName();
  }
  
  public String getFormId() {
    return rootElementDefn.formId;
  }
}