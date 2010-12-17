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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xform.util.XFormUtils;
import org.opendatakit.aggregate.constants.ParserConsts;
import org.opendatakit.aggregate.datamodel.DynamicBase;
import org.opendatakit.aggregate.datamodel.DynamicCommonFieldsBase;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.exception.ODKFormAlreadyExistsException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.exception.ODKParseException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData.Reason;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.FormDefinition;
import org.opendatakit.aggregate.form.FormInfo;
import org.opendatakit.aggregate.form.SubmissionAssociationTable;
import org.opendatakit.aggregate.form.XFormParameters;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.Realm;
import org.opendatakit.common.security.User;

/**
 * Parses an XML definition of an XForm based on java rosa types
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormParserForJavaRosa {

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

  private final Datastore datastore;
  private final User user;
  
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
   * creates the proper ODK Aggregate Form definition in the gae datastore.
   * 
   * @param formName - title of the form
   * @param formXmlData - Multipart form element defining the xml form...
   * @param inputXml - string containing the Xform definition
   * @param fileName - file name used for a file that specifies the form's XML definition
   * @param uploadedFormItems - Multipart form elements
   * @param datastore
   * @param user
   * @param rootDomain
   * @throws ODKFormAlreadyExistsException
   * @throws ODKIncompleteSubmissionData
   * @throws ODKConversionException
   * @throws ODKDatastoreException
   * @throws ODKParseException
   */
  public FormParserForJavaRosa(String formName, MultiPartFormItem formXmlData, String inputXml, String fileName,
	  MultiPartFormData uploadedFormItems,
      Datastore datastore, User user, Realm rootDomain) throws ODKFormAlreadyExistsException,
      ODKIncompleteSubmissionData, ODKConversionException, ODKDatastoreException,
      ODKParseException {

    if (inputXml == null || formXmlData == null) {
      throw new ODKIncompleteSubmissionData(Reason.MISSING_XML);
    }

    xml = inputXml;
    String strippedXML = JRHelperUtil.removeNonJavaRosaCompliantTags(xml);

    FormDef formDef;
    try {
      formDef = XFormUtils.getFormFromInputStream(new ByteArrayInputStream(strippedXML.getBytes()));
    } catch (Exception e) {
      throw new ODKIncompleteSubmissionData(e, Reason.BAD_JR_PARSE);
    }

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
    
    rootElementDefn = extractFormParameters( rootElement, schemaValue );
    if (rootElementDefn.formId == null) {
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

    // Determine the information about the submission...
    SubmissionProfile p = formDef.getSubmissionProfile();
    if ( p == null || p.getRef() == null ) {
    	submissionElement = rootElement;
    	submissionElementDefn = rootElementDefn;
    } else {
    	submissionElement = formDef.getInstance().resolveReference(p.getRef());
    	try {
    		submissionElementDefn = extractFormParameters( submissionElement, null );
    	} catch ( Exception e ) {
    		throw new ODKIncompleteSubmissionData(
    	            "The non-root submission element in the data model does not have an id attribute.  Add an id=\"your.domain.org:formId\" attribute to the submission element of your form.",
    	            Reason.ID_MISSING);
    	}
    }

    this.datastore = datastore;
    this.user = user;

    // And construct the base table prefix candidate from the submissionElementDefn.formId.
    // First, replace all slash substitutions with underscores.
    // Then replace all non-alphanumerics with underscores.
    String persistenceStoreFormId = submissionElementDefn.formId.substring(submissionElementDefn.formId.indexOf(':') + 1);
    persistenceStoreFormId = persistenceStoreFormId.replace(ParserConsts.FORWARD_SLASH_SUBSTITUTION, "_");
    persistenceStoreFormId = persistenceStoreFormId.replaceAll("[^\\p{Digit}\\p{javaUpperCase}\\p{javaLowerCase}]", "_");
    persistenceStoreFormId = persistenceStoreFormId.replaceAll("^_*","");
    // and then try to remove the realm prefix...
    {
    	List<String> alternates = new ArrayList<String>();
    	alternates.addAll(rootDomain.getDomains());
    	alternates.add(rootDomain.getRootDomain());
    	// make sure the collection is sorted in longest-string-first order.
    	// we want the longest domain name to 
    	Collections.sort(alternates, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				if ( o1.length() > o2.length() ) {
					return -1;
				} else if ( o1.length() < o2.length() ) {
					return 1;
				} else {
					return o1.compareTo(o2);
				}
			}
    	});

    	for ( String domain : alternates ) {
    		String mungedDomainName = domain.replaceAll("[^\\p{Digit}\\p{javaUpperCase}\\p{javaLowerCase}]", "_");
    	    if ( persistenceStoreFormId.startsWith(mungedDomainName) ) {
    	    	persistenceStoreFormId = persistenceStoreFormId.substring(mungedDomainName.length());
    	        persistenceStoreFormId = persistenceStoreFormId.replaceAll("^_*","");
    	        break;
    	    }
    	}
    }
    
    // OK -- we removed the organization's domain from what will be the 
    // database the table name prefix.  
    //
    
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

    initHelper(uploadedFormItems, formXmlData, inputXml,
    		  title, persistenceStoreFormId, formDef);
  }
  
  enum AuxType { NONE, VBN, VBN_REF, REF_BLOB, GEO_LAT, GEO_LNG, GEO_ALT, GEO_ACC, LONG_STRING_REF, REF_TEXT };
  
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
		  String inputXml, String title, String persistenceStoreFormId, 
		  FormDef formDef) throws ODKDatastoreException, ODKFormAlreadyExistsException, ODKParseException {
    
    /////////////////
    // Step 1: create or fetch the Form (FormInfo) submission
    //
    // This allows us to delete the form if upload goes bad...
    // create an empty submission then set values in it...
    Submission formInfo = Form.createOrFetchFormId(rootElementDefn.formId, datastore, user);
	// TODO: the following function throws an exception unless new or identical inputXml
    byte[] xmlBytes;
    try {
		xmlBytes = inputXml.getBytes(HtmlConsts.UTF8_ENCODE);
	} catch (UnsupportedEncodingException e) {
		throw new IllegalStateException("not reachable");
	}
    boolean sameXForm = FormInfo.setXFormDefinition( formInfo, 
    					rootElementDefn.modelVersion, rootElementDefn.uiVersion,
    					title, xmlBytes, datastore, user );

    FormInfo.setFormDescription( formInfo, null, title, null, null, datastore, user);

    Set<Map.Entry<String,MultiPartFormItem>> fileSet = uploadedFormItems.getFileNameEntrySet();
    for ( Map.Entry<String,MultiPartFormItem> itm : fileSet) {
    	if ( itm.getValue() == xformXmlData ) continue;// ignore the xform -- stored above.
    	FormInfo.setXFormMediaFile(formInfo,
    			rootElementDefn.modelVersion, rootElementDefn.uiVersion,
				itm.getValue(), datastore, user);
    }
    // Determine the information about the submission...
	FormInfo.setFormSubmission( formInfo, submissionElementDefn.formId, 
			submissionElementDefn.modelVersion, submissionElementDefn.uiVersion, datastore, user );
	formInfo.setIsComplete(true);
    formInfo.persist(datastore, user);

    SubmissionAssociationTable saRelation = SubmissionAssociationTable.createRelation(datastore, user);
    String submissionFormIdUri = CommonFieldsBase.newMD5HashUri(submissionElementDefn.formId); // key under which submission is located...
    Query q = datastore.createQuery(saRelation, user);
    q.addFilter( saRelation.domAuri, Query.FilterOperation.EQUAL, submissionFormIdUri);
    List<? extends CommonFieldsBase> l = q.executeQuery(0);
    SubmissionAssociationTable sa = null;
    fdmSubmissionUri = CommonFieldsBase.newUri();
    for ( CommonFieldsBase b : l ) {
    	SubmissionAssociationTable t = (SubmissionAssociationTable) b;
    	if ( t.getXFormParameters().equals(submissionElementDefn)) {
    		sa = t;
    		fdmSubmissionUri = sa.getUriSubmissionDataModel();
    		break;
    	}
    }
    
    if ( sa == null ) {
	    sa = datastore.createEntityUsingRelation(saRelation, user);
	    sa.setSubmissionFormId(submissionElementDefn.formId);
	    sa.setSubmissionModelVersion(submissionElementDefn.modelVersion);
	    sa.setSubmissionUiVersion(submissionElementDefn.uiVersion);
	    sa.setIsPersistenceModelComplete(false);
	    sa.setIsSubmissionAllowed(false);
	    sa.setUriSubmissionDataModel(fdmSubmissionUri);
	    sa.setDomAuri(submissionFormIdUri);
	    sa.setSubAuri(formInfo.getKey().getKey());
	    datastore.putEntity(sa, user);
    } else {
    	// the entry already exists...
    	if ( !sameXForm ) {
    		throw new ODKFormAlreadyExistsException();
    	}
    	// TODO: should do a transaction around persisting the FDM we are about to generate.
    	FormDefinition fd = FormDefinition.getFormDefinition(submissionElementDefn, datastore, user);
    	if ( fd != null ) return;
    }
    
    // so we have the formInfo record, but no data model backing it.
    // Find the submission associated with this form...
    
    final List<FormDataModel> fdmList = new ArrayList<FormDataModel>();

    final Set<CommonFieldsBase> createdRelations = new HashSet<CommonFieldsBase>();
    
    try {
	    //////////////////////////////////////////////////
	    // Step 2: Now build up the parse tree for the form...
	    //
	    final FormDataModel fdm = FormDataModel.createRelation(datastore, user);
	    
	    final EntityKey k = new EntityKey( fdm, fdmSubmissionUri);
	
	    NamingSet opaque = new NamingSet();
	
	    // construct the data model with table and column placeholders.
	    // assumes that the root is a non-repeating group element.
	    final String tableNamePlaceholder = opaque
	        .getTableName(fdm.getSchemaName(), persistenceStoreFormId, "", "CORE");
	
	    constructDataModel(opaque, k, fdmList, fdm, k.getKey(), 1, persistenceStoreFormId, "",
	        tableNamePlaceholder, submissionElement);
	
	    // emit the long string and ref text tables...
	    ++elementCount; // to give these tables their own element #.
	    String persistAsTable = opaque.getTableName(fdm.getSchemaName(), persistenceStoreFormId, "", "STRING_REF");
	    // long string ref text record...
	    FormDataModel d = datastore.createEntityUsingRelation(fdm, user);
	    setPrimaryKey( d, fdmSubmissionUri, AuxType.LONG_STRING_REF );
	    fdmList.add(d);
	    final String lstURI = d.getUri();
	    d.setOrdinalNumber(2L);
	    d.setTopLevelAuri(k.getKey());
	    d.setParentAuri(k.getKey());
	    d.setStringField(fdm.elementName, null);
	    d.setStringField(fdm.elementType, FormDataModel.ElementType.LONG_STRING_REF_TEXT.toString());
	    d.setStringField(fdm.persistAsColumn, null);
	    d.setStringField(fdm.persistAsTable, persistAsTable);
	    d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
	
	    persistAsTable = opaque.getTableName(fdm.getSchemaName(), persistenceStoreFormId, "", "STRING_TXT");
	    // ref text record...
	    d = datastore.createEntityUsingRelation(fdm, user);
	    setPrimaryKey( d, fdmSubmissionUri, AuxType.REF_TEXT );
	    fdmList.add(d);
	    d.setOrdinalNumber(1L);
	    d.setTopLevelAuri(k.getKey());
	    d.setParentAuri(lstURI);
	    d.setStringField(fdm.elementName, null);
	    d.setStringField(fdm.elementType, FormDataModel.ElementType.REF_TEXT.toString());
	    d.setStringField(fdm.persistAsColumn, null);
	    d.setStringField(fdm.persistAsTable, persistAsTable);
	    d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
	
	    // find a good set of names...
	    // this also ensures that the table names don't overlap existing tables
	    // in the datastore.
	    opaque.resolveNames(datastore, user);
	
	    // and revise the data model with those names...
	    for (FormDataModel m : fdmList) {
	      String tablePlaceholder = m.getPersistAsTable();
	      if (tablePlaceholder == null)
	        continue;
	
	      String columnPlaceholder = m.getPersistAsColumn();
	
	      String tableName = opaque.resolveTablePlaceholder(tablePlaceholder);
	      String columnName = opaque.resolveColumnPlaceholder(tablePlaceholder, columnPlaceholder);
	
	      if (!m.setStringField(m.persistAsColumn, columnName)) {
	        throw new IllegalArgumentException("overflow persistAsColumn");
	      }
	      if (!m.setStringField(m.persistAsTable, tableName)) {
	        throw new IllegalArgumentException("overflow persistAsTable");
	      }
	    }
	
	    for (FormDataModel m : fdmList) {
	      m.print(System.out);
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
		      FormDefinition fd = new FormDefinition(submissionElementDefn, fdmList);
		
		      createdRelations.add(fd.getLongStringRefTextTable());
		      createdRelations.add(fd.getRefTextTable());
		      
		      List<CommonFieldsBase> badTables = new ArrayList<CommonFieldsBase>();
		
		      for (CommonFieldsBase tbl : fd.getBackingTableSet()) {
		
		        try {
		        	datastore.assertRelation(tbl, user);
		        	createdRelations.add(tbl);
		        } catch (Exception e1) {
		          // assume it is because the table is too wide...
		          Logger.getLogger(FormParserForJavaRosa.class.getName()).warning(
		              "Create failed -- assuming phantom table required " + tbl.getSchemaName() + "."
		                  + tbl.getTableName());
		          try {
		            datastore.dropRelation(tbl, user);
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
		        orderlyDivideTable(fdmList, FormDataModel.createRelation(datastore, user), 
		        		tbl, opaque);
		      }
		
		      if (badTables.isEmpty())
		        break;
		      
		      // reset the derived fields so that the FormDefinition construction will work.
		      for ( FormDataModel m : fdmList ) {
		    	  m.resetDerivedFields();
		      }
		    }
	    } catch ( Exception e ) {
		      FormDefinition fd = new FormDefinition(submissionElementDefn, fdmList);
		  	
		      for (CommonFieldsBase tbl : fd.getBackingTableSet()) {
		    	  try {
		    		  datastore.dropRelation(tbl, user);
		    		  createdRelations.remove(tbl);
		    	  } catch ( Exception e3 ) {
		    		  // do nothing...
		    		  e3.printStackTrace();
		    	  }
		      }
		      if ( !createdRelations.isEmpty()) {
		    	  Logger.getLogger(FormParserForJavaRosa.class.getName()).severe(
		    			  "createdRelations not fully unwound!");
		    	  for (CommonFieldsBase tbl : createdRelations ) {
			    	  try {
				    	  Logger.getLogger(FormParserForJavaRosa.class.getName()).severe(
		    			  "--dropping " + tbl.getSchemaName() + "." + tbl.getTableName());
			    		  datastore.dropRelation(tbl, user);
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
    	List<EntityKey> keys = new ArrayList<EntityKey>();
    	formInfo.recursivelyAddEntityKeys(keys);
    	keys.add(new EntityKey(sa, sa.getUri()));
    	keys.add(formInfo.getKey());
    	datastore.deleteEntities(keys, user);
    	throw e;
    } catch ( ODKDatastoreException e ) {
    	List<EntityKey> keys = new ArrayList<EntityKey>();
    	formInfo.recursivelyAddEntityKeys(keys);
    	keys.add(new EntityKey(sa, sa.getUri()));
    	keys.add(formInfo.getKey());
    	datastore.deleteEntities(keys, user);
    	throw e;
    }

    //////////////////////////////////////////////
    // Step 4: record the data model...
    //
    // if we get here, we were able to create the tables -- record the
    // form description....
	datastore.putEntities(fdmList, user);
	
    // TODO: if above write fails, how do we clean this up?
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
      CommonFieldsBase tbl, NamingSet opaque) {
	  
    // Find out how many columns it has...
    int nCol = tbl.getFieldList().size() - DynamicCommonFieldsBase.WELL_KNOWN_COLUMN_COUNT;

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
          String newGroupTable = opaque.generateUniqueTableName(tbl.getSchemaName(), tbl.getTableName(),
        		  				datastore, user);
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
    String newPhantomTableName = opaque.generateUniqueTableName(tbl.getSchemaName(), tbl.getTableName(),
				datastore, user);
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
    FormDataModel d = datastore.createEntityUsingRelation(fdmRelation, user);
    fdmList.add(d);
    d.setStringField(fdmRelation.primaryKey, phantomURI);
    d.setOrdinalNumber(firstToMove.getOrdinalNumber());
    d.setTopLevelAuri(fdmSubmissionUri);
    d.setParentAuri(parentTable.getUri());
    d.setStringField(fdmRelation.elementName, null);
    d.setStringField(fdmRelation.elementType, FormDataModel.ElementType.PHANTOM.toString());
    d.setStringField(fdmRelation.persistAsColumn, null);
    d.setStringField(fdmRelation.persistAsTable, newPhantomTableName);
    d.setStringField(fdmRelation.persistAsSchema, fdmRelation.getSchemaName());

    // OK -- update ordinals and move remaining columns...
    long ordinalNumber = 0L;
    for ( ; idxStart < children.size() ; ++ idxStart ) {
    	FormDataModel m = children.get(idxStart);
        m.setParentAuri(phantomURI);
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
    
    if (!biggest.setStringField(biggest.persistAsTable, newPhantomTableName)) {
        throw new IllegalArgumentException("overflow of persistAsTable");
    }

	for (FormDataModel m : biggest.getChildren()) {
		recursivelyReassignChildren(m, tbl, newPhantomTableName);
    }

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
   * @return form element containing the needed info from the xform definition
   * @throws ODKEntityPersistException
   * @throws ODKParseException
   * 
   */

  private void constructDataModel(final NamingSet opaque, final EntityKey k,
      final List<FormDataModel> dmList, final FormDataModel fdm, 
      String parent, int ordinal, String tablePrefix, String nrGroupPrefix, String tableName,
      TreeElement treeElement) throws ODKEntityPersistException, ODKParseException {
    System.out.println("processing te: " + treeElement.getName() + " type: " + treeElement.dataType
        + " repeatable: " + treeElement.repeatable);

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
        persistAsColumn = null;
        opaque.removeColumnName(persistAsTable, persistAsColumn);
        et = FormDataModel.ElementType.REPEAT;
        persistAsTable = opaque.getTableName(fdm.getSchemaName(), tablePrefix, nrGroupPrefix,
            treeElement.getName());
      } else if (treeElement.getNumChildren() == 0) {
        // assume fields that don't have children are string fields.
        // the developer likely has not set a type for the field.
        et = FormDataModel.ElementType.STRING;
        Logger.getLogger(FormParserForJavaRosa.class.getCanonicalName()).warning(
            "Element " + getTreeElementPath(treeElement) + " does not have a type");
        throw new ODKParseException(
            "Field name: "
                + getTreeElementPath(treeElement)
                + " appears to be a value field (it has no fields nested within it) but does not have a type.");
      } else /* one or more children -- this is a non-repeating group */{
        persistAsColumn = null;
        opaque.removeColumnName(persistAsTable, persistAsColumn);
        et = FormDataModel.ElementType.GROUP;
      }
      break;

    default:
    case org.javarosa.core.model.Constants.DATATYPE_UNSUPPORTED:
      et = FormDataModel.ElementType.STRING;
      break;
    }

    // data record...
    d = datastore.createEntityUsingRelation(fdm, user);
    setPrimaryKey( d, fdmSubmissionUri, AuxType.NONE );
    dmList.add(d);
    final String groupURI = d.getUri();
    d.setOrdinalNumber(Long.valueOf(ordinal));
    d.setTopLevelAuri(k.getKey());
    d.setParentAuri(parent);
    d.setStringField(fdm.elementName, treeElement.getName());
    d.setStringField(fdm.elementType, et.toString());
    d.setStringField(fdm.persistAsColumn, persistAsColumn);
    d.setStringField(fdm.persistAsTable, persistAsTable);
    d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

    // and patch up the tree elements that have multiple fields...
    switch (et) {
    case BINARY:
      // binary elements have three additional tables associated with them
      // -- the _VBN, _REF and _BLB tables (in addition to _BIN above).
      persistAsTable = opaque.getTableName(fdm.getSchemaName(), tablePrefix, nrGroupPrefix,
          treeElement.getName() + "_VBN");

      // record for VersionedBinaryContent..
      d = datastore.createEntityUsingRelation(fdm, user);
	  setPrimaryKey( d, fdmSubmissionUri, AuxType.VBN );
      dmList.add(d);
      final String vbnURI = d.getUri();
      d.setOrdinalNumber(1L);
      d.setTopLevelAuri(k.getKey());
      d.setParentAuri(groupURI);
      d.setStringField(fdm.elementName, treeElement.getName());
      d.setStringField(fdm.elementType, FormDataModel.ElementType.VERSIONED_BINARY.toString());
      d.setStringField(fdm.persistAsColumn, null);
      d.setStringField(fdm.persistAsTable, persistAsTable);
      d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

      persistAsTable = opaque.getTableName(fdm.getSchemaName(), tablePrefix, nrGroupPrefix,
          treeElement.getName() + "_REF");

      // record for VersionedBinaryContentRefBlob..
      d = datastore.createEntityUsingRelation(fdm, user);
	  setPrimaryKey( d, fdmSubmissionUri, AuxType.VBN_REF );
	  dmList.add(d);
      final String bcbURI = d.getUri();
      d.setOrdinalNumber(1L);
      d.setTopLevelAuri(k.getKey());
      d.setParentAuri(vbnURI);
      d.setStringField(fdm.elementName, treeElement.getName());
      d.setStringField(fdm.elementType, FormDataModel.ElementType.VERSIONED_BINARY_CONTENT_REF_BLOB
          .toString());
      d.setStringField(fdm.persistAsColumn, null);
      d.setStringField(fdm.persistAsTable, persistAsTable);
      d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

      persistAsTable = opaque.getTableName(fdm.getSchemaName(), tablePrefix, nrGroupPrefix,
          treeElement.getName() + "_BLB");

      // record for RefBlob...
      d = datastore.createEntityUsingRelation(fdm, user);
	  setPrimaryKey( d, fdmSubmissionUri, AuxType.REF_BLOB );
	  dmList.add(d);
      d.setOrdinalNumber(1L);
      d.setTopLevelAuri(k.getKey());
      d.setParentAuri(bcbURI);
      d.setStringField(fdm.elementName, treeElement.getName());
      d.setStringField(fdm.elementType, FormDataModel.ElementType.REF_BLOB.toString());
      d.setStringField(fdm.persistAsColumn, null);
      d.setStringField(fdm.persistAsTable, persistAsTable);
      d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
      break;

    case GEOPOINT:
      // geopoints are stored as 4 fields (_LAT, _LNG, _ALT, _ACC) in the
      // persistence layer.
      // the geopoint attribute itself has no column, but is a placeholder
      // within
      // the data model for the expansion set of these 4 fields.

      persistAsColumn = opaque.getColumnName(persistAsTable, nrGroupPrefix, treeElement.getName()
          + "_LAT");

      d = datastore.createEntityUsingRelation(fdm, user);
	  setPrimaryKey( d, fdmSubmissionUri, AuxType.GEO_LAT );
      dmList.add(d);
      d.setOrdinalNumber(Long.valueOf(FormDataModel.GEOPOINT_LATITUDE_ORDINAL_NUMBER));
      d.setTopLevelAuri(k.getKey());
      d.setParentAuri(groupURI);
      d.setStringField(fdm.elementName, treeElement.getName());
      d.setStringField(fdm.elementType, FormDataModel.ElementType.DECIMAL.toString());
      d.setStringField(fdm.persistAsColumn, persistAsColumn);
      d.setStringField(fdm.persistAsTable, persistAsTable);
      d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

      persistAsColumn = opaque.getColumnName(persistAsTable, nrGroupPrefix, treeElement.getName()
          + "_LNG");

      d = datastore.createEntityUsingRelation(fdm, user);
	  setPrimaryKey( d, fdmSubmissionUri, AuxType.GEO_LNG );
      dmList.add(d);
      d.setOrdinalNumber(Long.valueOf(FormDataModel.GEOPOINT_LONGITUDE_ORDINAL_NUMBER));
      d.setTopLevelAuri(k.getKey());
      d.setParentAuri(groupURI);
      d.setStringField(fdm.elementName, treeElement.getName());
      d.setStringField(fdm.elementType, FormDataModel.ElementType.DECIMAL.toString());
      d.setStringField(fdm.persistAsColumn, persistAsColumn);
      d.setStringField(fdm.persistAsTable, persistAsTable);
      d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

      persistAsColumn = opaque.getColumnName(persistAsTable, nrGroupPrefix, treeElement.getName()
          + "_ALT");

      d = datastore.createEntityUsingRelation(fdm, user);
	  setPrimaryKey( d, fdmSubmissionUri, AuxType.GEO_ALT );
      dmList.add(d);
      d.setOrdinalNumber(Long.valueOf(FormDataModel.GEOPOINT_ALTITUDE_ORDINAL_NUMBER));
      d.setTopLevelAuri(k.getKey());
      d.setParentAuri(groupURI);
      d.setStringField(fdm.elementName, treeElement.getName());
      d.setStringField(fdm.elementType, FormDataModel.ElementType.DECIMAL.toString());
      d.setStringField(fdm.persistAsColumn, persistAsColumn);
      d.setStringField(fdm.persistAsTable, persistAsTable);
      d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

      persistAsColumn = opaque.getColumnName(persistAsTable, nrGroupPrefix, treeElement.getName()
          + "_ACC");

      d = datastore.createEntityUsingRelation(fdm, user);
	  setPrimaryKey( d, fdmSubmissionUri, AuxType.GEO_ACC );
      dmList.add(d);
      d.setOrdinalNumber(Long.valueOf(FormDataModel.GEOPOINT_ACCURACY_ORDINAL_NUMBER));
      d.setTopLevelAuri(k.getKey());
      d.setParentAuri(groupURI);
      d.setStringField(fdm.elementName, treeElement.getName());
      d.setStringField(fdm.elementType, FormDataModel.ElementType.DECIMAL.toString());
      d.setStringField(fdm.persistAsColumn, persistAsColumn);
      d.setStringField(fdm.persistAsTable, persistAsTable);
      d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
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
      for (int i = 0; i < treeElement.getNumChildren(); ++i) {
    	  TreeElement current = (TreeElement) treeElement.getChildAt(i);
    	  // TODO: make this pay attention to namespace of the tag...
    	  if ( (prior != null) && 
    		   (prior.getName().equals(current.getName())) ) {
    		  // it is the end-group tag...
    		  prior = current;
    	  } else {
    		  constructDataModel(opaque, k, dmList, fdm, groupURI, i + 1, tablePrefix,
    				  nrGroupPrefix, persistAsTable, current);
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
      for (int i = 0; i < treeElement.getNumChildren(); ++i) {
    	  TreeElement current = (TreeElement) treeElement.getChildAt(i);
    	  // TODO: make this pay attention to namespace of the tag...
    	  if ( (prior != null) && 
    		   (prior.getName().equals(current.getName())) ) {
    		  // it is the end-group tag...
    		  prior = current;
    	  } else {
    		  constructDataModel(opaque, k, dmList, fdm, groupURI, i + 1, tablePrefix,
    				  "", persistAsTable, current);
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