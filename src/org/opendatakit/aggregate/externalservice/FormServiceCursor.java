/**
 * Copyright (C) 2010 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.externalservice;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.externalservice.constants.ExternalServiceOption;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

public final class FormServiceCursor extends CommonFieldsBase {

	private static final String TABLE_NAME = "_form_service_cursor";
	/*
	 * Property Names for datastore
	 */
	private static final DataField SERVICE_CLASSNAME_PROPERTY = new DataField(
			"SERVICE_CLASSNAME", DataField.DataType.STRING, false, 200L);
	private static final DataField EXTERNAL_SERVICE_OPTION = new DataField(
			"EXTERNAL_SERVICE_OPTION", DataField.DataType.STRING, false, 80L);
	private static final DataField ESTABLISHMENT_DATETIME = new DataField(
			"ESTABLISHMENT_DATETIME", DataField.DataType.DATETIME, false);
	private static final DataField UPLOAD_COMPLETED_PROPERTY = new DataField(
			"UPLOAD_COMPLETED", DataField.DataType.BOOLEAN, true);
	private static final DataField LAST_UPLOAD_PERSISTENCE_CURSOR_PROPERTY = new DataField(
			"LAST_UPLOAD_PERSISTENCE_CURSOR", DataField.DataType.STRING, true,
			4096L);
	private static final DataField LAST_UPLOAD_KEY_PROPERTY = new DataField(
			"LAST_UPLOAD_KEY", DataField.DataType.STRING, true, 4096L);
	private static final DataField LAST_STREAMING_PERSISTENCE_CURSOR_PROPERTY = new DataField(
			"LAST_STREAMING_PERSISTENCE_CURSOR", DataField.DataType.STRING,
			true, 4096L);
	private static final DataField LAST_STREAMING_KEY_PROPERTY = new DataField(
			"LAST_STREAMING_KEY", DataField.DataType.STRING, true, 4096L);

	public final DataField serviceClassname;
	public final DataField externalServiceOption;
	public final DataField establishmentDateTime;
	public final DataField uploadCompleted;
	public final DataField lastUploadPersistenceCursor;
	public final DataField lastUploadKey;
	public final DataField lastStreamingPersistenceCursor;
	public final DataField lastStreamingKey;

	private FormServiceCursor(String schemaName) {
		super(schemaName, TABLE_NAME,
				CommonFieldsBase.BaseType.STATIC_ASSOCIATION);
		fieldList.add(serviceClassname = new DataField(
				SERVICE_CLASSNAME_PROPERTY));
		fieldList.add(externalServiceOption = new DataField(
				EXTERNAL_SERVICE_OPTION));
		fieldList.add(establishmentDateTime = new DataField(
				ESTABLISHMENT_DATETIME));
		fieldList
				.add(uploadCompleted = new DataField(UPLOAD_COMPLETED_PROPERTY));
		fieldList.add(lastUploadPersistenceCursor = new DataField(
				LAST_UPLOAD_PERSISTENCE_CURSOR_PROPERTY));
		fieldList.add(lastUploadKey = new DataField(LAST_UPLOAD_KEY_PROPERTY));
		fieldList.add(lastStreamingPersistenceCursor = new DataField(
				LAST_STREAMING_PERSISTENCE_CURSOR_PROPERTY));
		fieldList.add(lastStreamingKey = new DataField(
				LAST_STREAMING_KEY_PROPERTY));
	}

	// for creating empty rows
	public FormServiceCursor(FormServiceCursor ref) {
		super(ref);
		serviceClassname = ref.serviceClassname;
		externalServiceOption = ref.externalServiceOption;
		establishmentDateTime = ref.establishmentDateTime;
		uploadCompleted = ref.uploadCompleted;
		lastUploadPersistenceCursor = ref.lastUploadPersistenceCursor;
		lastUploadKey = ref.lastUploadKey;
		lastStreamingPersistenceCursor = ref.lastStreamingPersistenceCursor;
		lastStreamingKey = ref.lastStreamingKey;
	}

	public String getServiceClassname() {
		return getStringField(serviceClassname);
	}

	public void setServiceClassname(String value) {
		if (!setStringField(serviceClassname, value)) {
			throw new IllegalArgumentException("overflow serviceClassname");
		}
	}

	public ExternalServiceOption getExternalServiceOption() {
		return ExternalServiceOption
				.valueOf(getStringField(externalServiceOption));
	}

	public void setExternalServiceOption(ExternalServiceOption value) {
		if (!setStringField(externalServiceOption, value.toString())) {
			throw new IllegalArgumentException("overflow externalServiceOption");
		}
	}

	public Date getEstablishmentDateTime() {
		return getDateField(establishmentDateTime);
	}

	public void setEstablishmentDateTime(Date value) {
		setDateField(establishmentDateTime, value);
	}

	public Boolean getUploadCompleted() {
		return getBooleanField(uploadCompleted);
	}

	public void setUploadCompleted(Boolean value) {
		setBooleanField(uploadCompleted, value);
	}

	public String getLastUploadPersistenceCursor() {
		return getStringField(lastUploadPersistenceCursor);
	}

	public void setLastUploadPersistenceCursor(String value) {
		if (!setStringField(lastUploadPersistenceCursor, value)) {
			throw new IllegalArgumentException("overflow lastUploadPersistenceCursor");
		}
	}

	public String getLastUploadKey() {
		return getStringField(lastUploadKey);
	}

	public void setLastUploadKey(String value) {
		if (!setStringField(lastUploadKey, value)) {
			throw new IllegalArgumentException("overflow lastUploadKey");
		}
	}

	public String getLastStreamingPersistenceCursor() {
		return getStringField(lastStreamingPersistenceCursor);
	}

	public void setLastStreamingPersistenceCursor(String value) {
		if (!setStringField(lastStreamingPersistenceCursor, value)) {
			throw new IllegalArgumentException("overflow lastStreamingPersistenceCursor");
		}
	}

	public String getLastStreamingKey() {
		return getStringField(lastStreamingKey);
	}

	public void setLastStreamingKey(String value) {
		if (!setStringField(lastStreamingKey, value)) {
			throw new IllegalArgumentException("overflow lastStreamingKey");
		}
	}

	private static FormServiceCursor reference = null;

	public static final FormServiceCursor createRelation(Datastore ds, User user)
			throws ODKDatastoreException {
		if (reference == null) {
			// create the reference prototype using the schema of the form data
			// model object
			reference = new FormServiceCursor(ds.getDefaultSchemaName());
			ds.createRelation(reference, user);
		}
		return reference;
	}

	public static final FormServiceCursor createFormServiceCursor(
			EntityKey formKey, Class<?> serviceClass, CommonFieldsBase service,
			Datastore ds, User user) throws ODKDatastoreException {
		FormServiceCursor relationPrototype = createRelation(ds, user);

		FormServiceCursor c = ds.createEntityUsingRelation(relationPrototype, formKey,
				user);

		c.setDomAuri(formKey.getKey());
		c.setSubAuri(service.getUri());
		c.setServiceClassname(serviceClass.getName());

		return c;
	}

	public static final List<ExternalService> getExternalServicesForForm(
			EntityKey formKey, FormDefinition fd, Datastore ds, User user)
			throws ODKDatastoreException {
		FormServiceCursor relationPrototype = createRelation(ds, user);
		Query query = ds.createQuery(relationPrototype, user);
		query.addFilter(relationPrototype.domAuri, FilterOperation.EQUAL, formKey
				.getKey());
		List<ExternalService> esList = new ArrayList<ExternalService>();

		List<? extends CommonFieldsBase> fscList = query.executeQuery(0);
		for (CommonFieldsBase cb : fscList) {
			FormServiceCursor c = (FormServiceCursor) cb;
			Class<?> clazz = null;
			try {
				clazz = Class.forName(c.getServiceClassname());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				throw new IllegalStateException(
						"FormServiceCursor identifies a class "
								+ c.getServiceClassname()
								+ " that could not be found", e);
			}
			if (!ExternalService.class.isAssignableFrom(clazz)) {
				throw new IllegalStateException(
						"FormServiceCursor identifies a class that is not derived from ExternalService");
			}
			Class<?> paramTypes[] = new Class<?>[4];
			paramTypes[0] = FormDefinition.class;
			paramTypes[1] = FormServiceCursor.class;
			paramTypes[2] = Datastore.class;
			paramTypes[3] = String.class;
			Constructor<?> ec = null;
			try {
				ec = clazz.getConstructor(paramTypes);
			} catch (SecurityException e) {
				e.printStackTrace();
				throw new IllegalStateException("Constructor for "
						+ clazz.getCanonicalName() + " inaccessible!", e);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				throw new IllegalStateException("Constructor for "
						+ clazz.getCanonicalName() + " not found!", e);
			}
			Object argList[] = new Object[4];
			argList[0] = fd;
			argList[1] = c;
			argList[2] = ds;
			argList[3] = user.getUriUser();
			ExternalService obj;
			try {
				obj = (ExternalService) ec.newInstance(argList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new IllegalStateException("Constructor failed for "
						+ clazz.getCanonicalName(), e);
			}
			esList.add(obj);
		}
		return esList;
	}

}
