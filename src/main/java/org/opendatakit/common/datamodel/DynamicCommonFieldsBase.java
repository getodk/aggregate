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
package org.opendatakit.common.datamodel;

import java.util.Comparator;

import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.security.User;

/**
 * Common abstract class that unifies all TopLevelDynamic and Dynamic classes.
 * Primarily so that Submission can derive from SubmissionSet.  Useful for 
 * reconstruction of the FormInfo table definition.
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public abstract class DynamicCommonFieldsBase extends CommonFieldsBase {

  public static final Comparator<DynamicCommonFieldsBase> sameTableName = 
      new Comparator<DynamicCommonFieldsBase>(){

    @Override
    public int compare(DynamicCommonFieldsBase arg0, DynamicCommonFieldsBase arg1) {
      int cmp = 0;
      if ( arg0.sameTable(arg1) ) {
        return 0;
      }
      cmp = arg0.getSchemaName().compareTo(arg1.getSchemaName());
      if ( cmp != 0 ) {
        return cmp;
      }
      cmp = arg0.getTableName().compareTo(arg1.getTableName());
      return cmp;
    }};
    
	/**
	 * Construct a relation prototype.
	 * 
	 * @param databaseSchema
	 * @param tableName
	 * @param type
	 */
	protected DynamicCommonFieldsBase(String databaseSchema, String tableName) {
		super(databaseSchema, tableName);
	}

	/**
	 * Construct an empty entity.
	 * 
	 * @param ref
	 * @param user
	 */
	protected DynamicCommonFieldsBase(CommonFieldsBase ref, User user) {
		super(ref, user);
	}
	
	/**
	 * Add a field to the field list.  Needed for dynamic form construction.
	 * 
	 * @param d
	 */
	public void addDataField(DataField d) {
		fieldList.add(d);
	}
}
