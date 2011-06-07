package org.opendatakit.aggregate.odktables.relation;

import java.util.Collections;
import java.util.List;

import org.opendatakit.common.ermodel.AbstractRelationAdapter;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Table represents a table stored in the datastore. Every Table must be indexed
 * in the TableIndex.
 * </p>
 * 
 * <p>
 * Tables can not be instantiated directly, instead they should be managed
 * through the {@link TableIndex#createTable createTable},
 * {@link TableIndex#getTable getTable}, and {@link TableIndex#deleteTable
 * deleteTable} methods in TableIndex.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Table extends AbstractRelationAdapter
{
    private List<DataField> fields;

    /**
     * Constructs a new Table. If the constructed Table does not already exist
     * it will be created in the datastore.
     * 
     * @param namespace
     *            the unique identifier of the Table. Must consist of uppercase
     *            letters, numbers, and underscores and must begin with an
     *            uppercase letter. namespace.length + tableId.length must not
     *            be greater than 60 characters.
     * @param tableId
     *            the unique identifier of the Table. Must consist of uppercase
     *            letters, numbers, and underscores and must begin with an
     *            uppercase letter. namespace.length + tableId.length must not
     *            be greater than 60 characters.
     * @param tableFields
     *            a list of DataFields representing the fields of the Table
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was a problem during communication with the
     *             datastore
     */
    protected Table(String namespace, String tableId,
            List<DataField> tableFields, CallingContext cc)
            throws ODKDatastoreException
    {
        super(namespace, tableId, tableFields, cc);
        this.fields = tableFields;
    }
    
    /**
     * @return a list of DataFields representing the columns of this table.
     */
    public List<DataField> getDataFields()
    {
        return Collections.unmodifiableList(this.fields);
    }
}