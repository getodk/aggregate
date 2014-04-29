/*
 * Copyright (C) 2012-2013 University of Washington
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

package org.opendatakit.aggregate.odktables.relation;

import java.util.Locale;

import org.opendatakit.common.ermodel.AbstractBlobRelationSet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * This represents the datastore table that holds files for the instance data
 * of a table. There is one of these for each table in order to simplify
 * DBA operations.
 * <p>
 * These files are going to be stored using an AbstractBlobRelationSet. This
 * handles most of the mechanics of storing arbitrarily large binary files. It
 * is based on BlobRelationSetTest.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class DbTableInstanceFiles extends AbstractBlobRelationSet {

  public DbTableInstanceFiles(String tableId, CallingContext cc) throws ODKDatastoreException {
    super(tableId.toUpperCase(Locale.ENGLISH) + "_ATT", cc);
  }
}
