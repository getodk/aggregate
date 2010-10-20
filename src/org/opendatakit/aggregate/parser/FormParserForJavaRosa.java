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
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xform.util.XFormUtils;
import org.opendatakit.aggregate.constants.ParserConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.exception.ODKFormAlreadyExistsException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.exception.ODKParseException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData.Reason;
import org.opendatakit.aggregate.form.FormInfo;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.StringSubmissionType;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.InstanceDataBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;

/**
 * Parses an XML definition of an XForm based on java rosa types
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class FormParserForJavaRosa {

	/**
	 * The ODK Id that uniquely identifies the form
	 */
	private String odkId = null; // needs to be set to null initially for the
	// logic to work;

	/**
	 * The XForm definition in XML
	 */
	private String xml;

	private Datastore ds;

	/**
	 * Constructor that parses and xform from the input stream supplied and
	 * creates the proper ODK Aggregate Form definition in the gae datastore
	 * 
	 * @param formName
	 *            name of xform to be parsed
	 * @param inputXml
	 *            input stream containing the Xform definition
	 * @param fileName
	 *            file name used for a file that specifies the form's XML
	 *            definition
	 * @param datastore
	 *            TODO
	 * @param uriUser
	 * 
	 * @throws ODKFormAlreadyExistsException
	 * @throws ODKConversionException
	 * @throws ODKDatastoreException
	 * @throws ODKParseException 
	 * @throws UnsupportedEncodingException 
	 */
	public FormParserForJavaRosa(String formName, String inputXml,
			String fileName, Datastore datastore, User user, String rootDomain)
			throws ODKFormAlreadyExistsException, ODKIncompleteSubmissionData,
			ODKConversionException, ODKDatastoreException, ODKParseException, UnsupportedEncodingException {

		xml = inputXml;
		String strippedXML = JRHelperUtil.removeNonJavaRosaCompliantTags(xml);
		FormDef formDef = XFormUtils
				.getFormFromInputStream(new ByteArrayInputStream(strippedXML
						.getBytes()));

		// TODO: figure out a better way to handle this situation
		if (formDef == null) {
			throw new ODKParseException("Javarosa was unable to parse the xform.");
		}

		DataModelTree dataModel = formDef.getDataModel();
		TreeElement rootElement = dataModel.getRoot();

		// obtain form id
		// first search for the "id" attribute
		if (rootElement != null) {
			for (int i = 0; i < rootElement.getAttributeCount(); i++) {
				String name = rootElement.getAttributeName(i);
				if (name.equals(ParserConsts.ODK_ATTRIBUTE_NAME)) {
					odkId = rootElement.getAttributeValue(i);
					break;
				}
			}
		}

		// second check for the xmlns being defined
		// otherwise throw exception
		if (odkId == null) {
			if (dataModel.schema == null) {
				throw new ODKIncompleteSubmissionData(Reason.ID_MISSING);
			}
			String httpPrefix = "http://";
			String httpsPrefix = "https://";
			
			odkId = dataModel.schema;
			if ( dataModel.schema.startsWith(httpPrefix) ) {
				odkId = dataModel.schema.substring(httpPrefix.length());
			} else if ( dataModel.schema.startsWith(httpsPrefix) ) {
				odkId = dataModel.schema.substring(httpsPrefix.length());
			}
			odkId = odkId.replaceAll("[^\\p{Digit}\\p{javaUpperCase}\\p{javaLowerCase}]", "_");
		}

		ds = datastore;

		int firstSlash = odkId.indexOf('/');
		if (firstSlash != -1) {
			odkId = odkId.substring(0, firstSlash);
		}
		if (odkId.indexOf(':') == -1) {
			odkId = rootDomain + ":" + odkId;
		}
		String formId = odkId.substring(odkId.indexOf(':') + 1);

		FormDefinition fd = FormDefinition.getFormDefinition(odkId, ds, user);
		if (fd != null) {
			throw new ODKFormAlreadyExistsException();
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
		title = title.replace(BasicConsts.FORWARDSLASH,
				BasicConsts.EMPTY_STRING);

		final FormDataModel fdm = FormDefinition.getFormDataModel(ds, user);

		final List<FormDataModel> fdmList = new ArrayList<FormDataModel>();

		// define the form name ...
		FormDataModel d = ds.createEntityUsingRelation(fdm, null, user);
		fdmList.add(d);
		final String topLevelURI = d.getUri();
		d.setStringField(fdm.primaryKey, topLevelURI);
		d.setOrdinalNumber(1L);
		d.setParentAuri(null);
		d.setStringField(fdm.uriFormId, odkId);
		d.setStringField(fdm.elementName, title);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.FORM_NAME
				.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, null);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
		final EntityKey k = new EntityKey(d, d.getUri());

		NamingSet opaque = new NamingSet();

		// construct the data model with table and column placeholders.
		// assumes that the root is a non-repeating group element.
		final String tableNamePlaceholder = opaque.getTableName(fdm
				.getSchemaName(), formId, "", "CORE");

		constructDataModel(opaque, k, fdmList, fdm, user, odkId,
				topLevelURI, 1, formId, "", tableNamePlaceholder, dataModel
						.getRoot());

		// emit the long string and ref text tables...

		String persistAsTable = opaque.getTableName(fdm.getSchemaName(),
				formId, "", "STRING_REF");
		// long string ref text record...
		d = ds.createEntityUsingRelation(fdm, k, user);
		fdmList.add(d);
		final String lstURI = d.getUri();
		d.setOrdinalNumber(2L);
		d.setParentAuri(topLevelURI);
		d.setStringField(fdm.uriFormId, odkId);
		d.setStringField(fdm.elementName, null);
		d.setStringField(fdm.elementType,
				FormDataModel.ElementType.LONG_STRING_REF_TEXT.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, persistAsTable);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		persistAsTable = opaque.getTableName(fdm.getSchemaName(), formId, "",
				"STRING_TXT");
		// ref text record...
		d = ds.createEntityUsingRelation(fdm, k, user);
		fdmList.add(d);
		d.setOrdinalNumber(1L);
		d.setParentAuri(lstURI);
		d.setStringField(fdm.uriFormId, odkId);
		d.setStringField(fdm.elementName, null);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.REF_TEXT
				.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, persistAsTable);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

		// find a good set of names...
		// this also ensures that the table names don't overlap existing tables
		// in the datastore.
		opaque.resolveNames(ds, user);

		// and revise the data model with those names...
		for (FormDataModel m : fdmList) {
			String tablePlaceholder = m.getPersistAsTable();
			if (tablePlaceholder == null)
				continue;

			String columnPlaceholder = m.getPersistAsColumn();

			String tableName = opaque.resolveTablePlaceholder(tablePlaceholder);
			String columnName = opaque.resolveColumnPlaceholder(
					tablePlaceholder, columnPlaceholder);

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
		for (;;) {
			fd = new FormDefinition(odkId, fdmList);

			List<CommonFieldsBase> badTables = new ArrayList<CommonFieldsBase>();

			for (Map.Entry<String, CommonFieldsBase> e : fd.backingTableMap
					.entrySet()) {
				CommonFieldsBase tbl = e.getValue();

				try {
					ds.createRelation(tbl, user);
				} catch (Exception e1) {
					// assume it is because the table is too wide...
					Logger.getLogger(FormParserForJavaRosa.class.getName())
							.warning(
									"Create failed -- assuming phantom table required "
											+ tbl.getSchemaName() + "."
											+ tbl.getTableName());
					try {
						ds.deleteRelation(tbl, user);
					} catch (Exception e2) {
						// no-op
					}
					badTables.add(tbl);
				}
			}

			for (CommonFieldsBase tbl : badTables) {
				// dang. We need to create phantom tables...
				String newPhantom = opaque.generateUniqueTableName(tbl
						.getSchemaName(), tbl.getTableName(), ds, user);

				orderlyDivideTable(fdmList, FormDefinition.getFormDataModel(ds, user), user,
						odkId, fd, tbl, newPhantom);
			}

			if (badTables.isEmpty())
				break;
		}

		// if we get here, we were able to create the tables -- record the
		// form description....
		ds.putEntities(fdmList, user);

		// and write the FormInfo record...

		// create an empty submission then set values in it...
		FormDefinition formInfoDefn = FormInfo.getFormDefinition(ds);
		
		Submission formInfo = new Submission(formInfoDefn, ds, user);

		((StringSubmissionType) formInfo.getElementValue(FormInfo.formId))
				.setValueFromString(odkId);
		BlobSubmissionType bt = (BlobSubmissionType) formInfo
				.getElementValue(FormInfo.formBinaryContent);
		byte[] byteArray = inputXml.getBytes();
		bt.setValueFromByteArray(byteArray, "text/xml", Long
				.valueOf(byteArray.length), title + ".xml", datastore, user);

		// TODO: handle media files
		formInfo.persist(ds, user);
	}

	private void orderlyDivideTable(List<FormDataModel> fdmList,
			FormDataModel fdm, User user, String odkId2,
			FormDefinition fd, CommonFieldsBase tbl, String newPhantomTableName) {

		if (!(tbl instanceof InstanceDataBase)) {
			throw new IllegalArgumentException(
					"Yikes! Don't expect this to be a non-form table");
		}
		// OK. It is a table for holding xform data.

		// Find out how many columns it has...
		int nCol = tbl.getFieldList().size()
				- InstanceDataBase.WELL_KNOWN_COLUMN_COUNT;

		if (nCol < 2) {
			throw new IllegalStateException(
					"Unable to subdivide instance table! "
							+ tbl.getSchemaName() + "." + tbl.getTableName());
		}

		// find the entry corresponding to this parent table.
		FormDataModel parentTable = null;
		for (FormDataModel m : fdmList) {
			if (m.getPersistAsColumn() != null)
				continue;
			String table = m.getPersistAsTable();
			if ( table == null ) continue; // FORM_NAME...
			if (table.equals(tbl.getTableName())
					&& m.getPersistAsSchema().equals(tbl.getSchemaName())) {
				parentTable = m; // anything we find is good enough...
				break;
			}
		}
		// we should have found something...
		if ( parentTable == null ) {
			throw new IllegalStateException("Unable to locate model for backing table");
		}
		
		// ensure that it is the highest model element with this backing object.
		while (parentTable.getParent() != null) {
			FormDataModel parent = parentTable.getParent();
			if (!tbl.equals(parent.getBackingObjectPrototype()))
				break;
			// daisy-chain up to parent
			// we must have had a subordinate group...
			parentTable = parent;
		}

		// go through the fdmList identifying the entries immediately below
		// the parent that are backed by the table we need to split.
		List<FormDataModel> topElementChange = new ArrayList<FormDataModel>();
		List<FormDataModel> groups = new ArrayList<FormDataModel>();
		for (;;) {
			for (FormDataModel m : fdmList) {
				if (tbl.equals(m.getBackingObjectPrototype())) {
					if (m != parentTable) {
						if (parentTable.getUri().equals(m.getParentAuri())) {
							if (!m.getElementType().equals(
									FormDataModel.ElementType.GROUP)) {
								topElementChange.add(m);
							} else {
								groups.add(m);
							}
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
							"Failure in create table when there are no nested groups!");
				}
			} else {
				// OK we have a chance to do something at this level...
				break;
			}
		}

		if (groups.size() > 0 && topElementChange.size() > 1) {
			// add phantom table for largest group -- since flec > 1, we know
			// that other columns will remain in the original table...
			// the nice thing is, this doesn't require any new fdm records!
			int biggestCount = 0;
			FormDataModel biggest = null;
			for (FormDataModel m : groups) {
				int count = recursivelyCountChildrenInSameTable(m);
				if (biggestCount <= count) {
					biggestCount = count;
					biggest = m;
				}
			}
			// reassign biggest and its children, etc. to the new table...
			recursivelyReassignChildren(biggest, newPhantomTableName);

			// and try it again now...
			return;
		}

		// Urgh! we don't have a nested group we can cleave off or it is all
		// that is in this table.
		// We will need to subdivide this manually by creating a phantom
		// table...
		String phantomURI = CommonFieldsBase.newUri();

		// OK -- attempt to split the column count in half...
		long desiredOriginalTableColCount = (nCol / 2);
		for (FormDataModel m : topElementChange) {
			long newOrdinal = m.getOrdinalNumber()
					- desiredOriginalTableColCount;
			if (newOrdinal > 0) {
				m.setParentAuri(phantomURI);
				m.setOrdinalNumber(newOrdinal);
				recursivelyReassignChildren(m, newPhantomTableName);
			}
		}

		// data record...
		FormDataModel d = ds.createEntityUsingRelation(fdm, new EntityKey(fdm,
				fdmList.get(0).getUri()), user);
		fdmList.add(d);
		d.setStringField(fdm.primaryKey, phantomURI);
		d.setOrdinalNumber(Long.valueOf(desiredOriginalTableColCount + 1));
		d.setParentAuri(parentTable.getUri());
		d.setStringField(fdm.uriFormId, odkId);
		d.setStringField(fdm.elementName, null);
		d.setStringField(fdm.elementType, FormDataModel.ElementType.PHANTOM
				.toString());
		d.setStringField(fdm.persistAsColumn, null);
		d.setStringField(fdm.persistAsTable, newPhantomTableName);
		d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());
	}

	private int recursivelyCountChildrenInSameTable(FormDataModel parent) {

		int count = 0;
		for (FormDataModel m : parent.getChildren()) {
			if (parent.getTableName().equals(m.getPersistAsTable())
					&& parent.getSchemaName().equals(m.getSchemaName())) {
				count += recursivelyCountChildrenInSameTable(m);
			}
		}
		if (parent.getPersistAsColumn() != null) {
			count++;
		}
		return count;
	}

	private void recursivelyReassignChildren(FormDataModel biggest,
			String newPhantomTableName) {

		for (FormDataModel m : biggest.getChildren()) {
			if (biggest.getTableName().equals(m.getPersistAsTable())
					&& biggest.getSchemaName().equals(m.getSchemaName())) {
				recursivelyReassignChildren(m, newPhantomTableName);
			}
		}

		if (!biggest
				.setStringField(biggest.persistAsTable, newPhantomTableName)) {
			throw new IllegalArgumentException("overflow of persistAsTable");
		}
	}

	/**
	 * Used to recursively process the xform definition tree to create the form
	 * data model.
	 * 
	 * @param treeElement
	 *            java rosa tree element
	 * 
	 * @param parentKey
	 *            key from the parent form for proper entity group usage in gae
	 * 
	 * @param parent
	 *            parent form element
	 * 
	 * @return form element containing the needed info from the xform definition
	 * @throws ODKEntityPersistException
	 * @throws ODKParseException 
	 * 
	 */

	private void constructDataModel(final NamingSet opaque,
			final EntityKey k, final List<FormDataModel> dmList,
			final FormDataModel fdm, final User user, String odkId,
			String parent, int ordinal, String tablePrefix,
			String nrGroupPrefix, String tableName, TreeElement treeElement)
			throws ODKEntityPersistException, ODKParseException {
		System.out.println("processing te: " + treeElement.getName()
				+ " type: " + treeElement.dataType + " repeatable: "
				+ treeElement.repeatable);

		FormDataModel d;

		FormDataModel.ElementType et;
		String persistAsTable = tableName;
		String originalPersistAsColumn = opaque.getColumnName(persistAsTable,
				nrGroupPrefix, treeElement.getName());
		String persistAsColumn = originalPersistAsColumn;

		switch (treeElement.dataType) {
		case org.javarosa.core.model.Constants.DATATYPE_TEXT:/**
			 * Text question
			 * type.
			 */
			et = FormDataModel.ElementType.STRING;
			break;
		case org.javarosa.core.model.Constants.DATATYPE_INTEGER:/**
			 * Numeric
			 * question type. These are numbers without decimal points
			 */
			et = FormDataModel.ElementType.INTEGER;
			break;
		case org.javarosa.core.model.Constants.DATATYPE_DECIMAL:/**
			 * Decimal
			 * question type. These are numbers with decimals
			 */
			et = FormDataModel.ElementType.DECIMAL;
			break;
		case org.javarosa.core.model.Constants.DATATYPE_DATE:/**
			 * Date question
			 * type. This has only date component without time.
			 */
			et = FormDataModel.ElementType.JRDATE;
			break;
		case org.javarosa.core.model.Constants.DATATYPE_TIME:/**
			 * Time question
			 * type. This has only time element without date
			 */
			et = FormDataModel.ElementType.JRTIME;
			break;
		case org.javarosa.core.model.Constants.DATATYPE_DATE_TIME:/**
			 * Date and
			 * Time question type. This has both the date and time components
			 */
			et = FormDataModel.ElementType.JRDATETIME;
			break;
		case org.javarosa.core.model.Constants.DATATYPE_CHOICE:/**
			 * This is a
			 * question with alist of options where not more than one option can
			 * be selected at a time.
			 */
			et = FormDataModel.ElementType.STRING;
//			et = FormDataModel.ElementType.SELECT1;
//			persistAsColumn = null;
//			persistAsTable = opaque.getTableName(fdm.getSchemaName(),
//					tablePrefix, nrGroupPrefix, treeElement.getName());
			break;
		case org.javarosa.core.model.Constants.DATATYPE_CHOICE_LIST:/**
			 * This is a
			 * question with alist of options where more than one option can be
			 * selected at a time.
			 */
			et = FormDataModel.ElementType.SELECTN;
			opaque.removeColumnName(persistAsTable, persistAsColumn);
			persistAsColumn = null;
			persistAsTable = opaque.getTableName(fdm.getSchemaName(),
					tablePrefix, nrGroupPrefix, treeElement.getName());
			break;
		case org.javarosa.core.model.Constants.DATATYPE_BOOLEAN:/**
			 * Question with
			 * true and false answers.
			 */
			et = FormDataModel.ElementType.BOOLEAN;
			break;
		case org.javarosa.core.model.Constants.DATATYPE_GEOPOINT:/**
			 * Question
			 * with location answer.
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
			persistAsTable = opaque.getTableName(fdm.getSchemaName(),
					tablePrefix, nrGroupPrefix, treeElement.getName() + "_BN");
			break;

		case org.javarosa.core.model.Constants.DATATYPE_NULL: /*
															 * for nodes that
															 * have no data, or
															 * data type
															 * otherwise unknown
															 */
			if (treeElement.repeatable) {
				persistAsColumn = null;
				opaque.removeColumnName(persistAsTable, persistAsColumn);
				et = FormDataModel.ElementType.REPEAT;
				persistAsTable = opaque.getTableName(fdm.getSchemaName(),
						tablePrefix, nrGroupPrefix, treeElement.getName());
			} else if ( treeElement.getNumChildren() == 0 ) {
				// assume fields that don't have children are string fields.
				// the developer likely has not set a type for the field.
				et = FormDataModel.ElementType.STRING;
				Logger.getLogger(FormParserForJavaRosa.class.getCanonicalName()).warning(
						"Element " + treeElement.getName() + " does not have a type");
				throw new ODKParseException("Field name: " + treeElement.getName() + 
						" appears to be a value field (it has no fields nested within it) but does not have a type.");
			} else /* one or more children -- this is a non-repeating group */ {
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
		d = ds.createEntityUsingRelation(fdm, k, user);
		dmList.add(d);
		final String groupURI = d.getUri();
		d.setOrdinalNumber(Long.valueOf(ordinal));
		d.setParentAuri(parent);
		d.setStringField(fdm.uriFormId, odkId);
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
			persistAsTable = opaque.getTableName(fdm.getSchemaName(),
					tablePrefix, nrGroupPrefix, treeElement.getName() + "_VBN");

			// record for VersionedBinaryContent..
			d = ds.createEntityUsingRelation(fdm, k, user);
			dmList.add(d);
			final String vbnURI = d.getUri();
			d.setOrdinalNumber(1L);
			d.setParentAuri(groupURI);
			d.setStringField(fdm.uriFormId, odkId);
			d.setStringField(fdm.elementName, treeElement.getName());
			d.setStringField(fdm.elementType,
					FormDataModel.ElementType.VERSIONED_BINARY.toString());
			d.setStringField(fdm.persistAsColumn, null);
			d.setStringField(fdm.persistAsTable, persistAsTable);
			d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

			persistAsTable = opaque.getTableName(fdm.getSchemaName(),
					tablePrefix, nrGroupPrefix, treeElement.getName() + "_REF");

			// record for VersionedBinaryContentRefBlob..
			d = ds.createEntityUsingRelation(fdm, k, user);
			dmList.add(d);
			final String bcbURI = d.getUri();
			d.setOrdinalNumber(1L);
			d.setParentAuri(vbnURI);
			d.setStringField(fdm.uriFormId, odkId);
			d.setStringField(fdm.elementName, treeElement.getName());
			d.setStringField(fdm.elementType,
					FormDataModel.ElementType.VERSIONED_BINARY_CONTENT_REF_BLOB
							.toString());
			d.setStringField(fdm.persistAsColumn, null);
			d.setStringField(fdm.persistAsTable, persistAsTable);
			d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

			persistAsTable = opaque.getTableName(fdm.getSchemaName(),
					tablePrefix, nrGroupPrefix, treeElement.getName() + "_BLB");

			// record for RefBlob...
			d = ds.createEntityUsingRelation(fdm, k, user);
			dmList.add(d);
			d.setOrdinalNumber(1L);
			d.setParentAuri(bcbURI);
			d.setStringField(fdm.uriFormId, odkId);
			d.setStringField(fdm.elementName, treeElement.getName());
			d.setStringField(fdm.elementType,
					FormDataModel.ElementType.REF_BLOB.toString());
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

			persistAsColumn = opaque.getColumnName(persistAsTable,
					nrGroupPrefix, treeElement.getName() + "_LAT");

			d = ds.createEntityUsingRelation(fdm, k, user);
			dmList.add(d);
			d.setOrdinalNumber(Long
					.valueOf(FormDataModel.GEOPOINT_LATITUDE_ORDINAL_NUMBER));
			d.setParentAuri(groupURI);
			d.setStringField(fdm.uriFormId, odkId);
			d.setStringField(fdm.elementName, treeElement.getName());
			d.setStringField(fdm.elementType, FormDataModel.ElementType.DECIMAL
					.toString());
			d.setStringField(fdm.persistAsColumn, persistAsColumn);
			d.setStringField(fdm.persistAsTable, persistAsTable);
			d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

			persistAsColumn = opaque.getColumnName(persistAsTable,
					nrGroupPrefix, treeElement.getName() + "_LNG");

			d = ds.createEntityUsingRelation(fdm, k, user);
			dmList.add(d);
			d.setOrdinalNumber(Long
					.valueOf(FormDataModel.GEOPOINT_LONGITUDE_ORDINAL_NUMBER));
			d.setParentAuri(groupURI);
			d.setStringField(fdm.uriFormId, odkId);
			d.setStringField(fdm.elementName, treeElement.getName());
			d.setStringField(fdm.elementType, FormDataModel.ElementType.DECIMAL
					.toString());
			d.setStringField(fdm.persistAsColumn, persistAsColumn);
			d.setStringField(fdm.persistAsTable, persistAsTable);
			d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

			persistAsColumn = opaque.getColumnName(persistAsTable,
					nrGroupPrefix, treeElement.getName() + "_ALT");

			d = ds.createEntityUsingRelation(fdm, k, user);
			dmList.add(d);
			d.setOrdinalNumber(Long
					.valueOf(FormDataModel.GEOPOINT_ALTITUDE_ORDINAL_NUMBER));
			d.setParentAuri(groupURI);
			d.setStringField(fdm.uriFormId, odkId);
			d.setStringField(fdm.elementName, treeElement.getName());
			d.setStringField(fdm.elementType, FormDataModel.ElementType.DECIMAL
					.toString());
			d.setStringField(fdm.persistAsColumn, persistAsColumn);
			d.setStringField(fdm.persistAsTable, persistAsTable);
			d.setStringField(fdm.persistAsSchema, fdm.getSchemaName());

			persistAsColumn = opaque.getColumnName(persistAsTable,
					nrGroupPrefix, treeElement.getName() + "_ACC");

			d = ds.createEntityUsingRelation(fdm, k, user);
			dmList.add(d);
			d.setOrdinalNumber(Long
					.valueOf(FormDataModel.GEOPOINT_ACCURACY_ORDINAL_NUMBER));
			d.setParentAuri(groupURI);
			d.setStringField(fdm.uriFormId, odkId);
			d.setStringField(fdm.elementName, treeElement.getName());
			d.setStringField(fdm.elementType, FormDataModel.ElementType.DECIMAL
					.toString());
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
			for (int i = 0; i < treeElement.getNumChildren(); ++i) {
				constructDataModel(opaque, k, dmList, fdm, user, odkId,
						groupURI, i + 1, tablePrefix, nrGroupPrefix,
						persistAsTable, (TreeElement) treeElement.getChildren()
								.get(i));
			}
			break;

		case REPEAT:
			// repeating group - clears group prefix
			// and all children are emitted.
			for (int i = 0; i < treeElement.getNumChildren(); ++i) {
				constructDataModel(opaque, k, dmList, fdm, user, odkId,
						groupURI, i + 1, tablePrefix, "", persistAsTable,
						(TreeElement) treeElement.getChildren().get(i));
			}
			break;
		}
	}

	public String getFormId() {
		// TODO Auto-generated method stub
		return odkId;
	}
}