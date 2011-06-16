package org.opendatakit.aggregate.odktables.relation;

import java.util.Collections;
import java.util.List;

import org.opendatakit.aggregate.odktables.entities.Row;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Table represents a table stored in the datastore. Every Table must be indexed
 * in the TableIndex.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Rows extends TypedEntityRelation<Row>
{
    /**
     * The namespace for Rows relations.
     */
    public static final String NAMESPACE = "ODKTABLES";

    private List<DataField> fields;

    /**
     * Constructs a Table. If the constructed Table does not already exist in
     * the datastore it will be created.
     * 
     * @param namespace
     *            the namespace the table should be created under.
     * @param tableUri
     *            the globally unique identifier of the Table.
     * @param tableFields
     *            a list of DataFields representing the fields of the Table
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was a problem during communication with the
     *             datastore
     */
    private Rows(String namespace, String tableUri,
            List<DataField> tableFields, CallingContext cc)
            throws ODKDatastoreException
    {
        super(namespace, tableUri, tableFields, cc);
        this.fields = tableFields;
    }

    @Override
    public Row initialize(Entity entity) throws ODKDatastoreException
    {
        return new Row(this, entity);
    }

    /**
     * @return a list of DataFields representing the columns of this table.
     */
    public List<DataField> getDataFields()
    {
        return Collections.unmodifiableList(this.fields);
    }

    public static Rows getInstance(String tableUri, CallingContext cc)
            throws ODKDatastoreException
    {
        List<DataField> tableFields = Columns.getInstance(cc).getDataFields(
                tableUri);
        return new Rows(NAMESPACE, tableUri, tableFields, cc);
    }
}