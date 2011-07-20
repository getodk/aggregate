package org.opendatakit.aggregate.odktables.relation;

import java.util.Collections;
import java.util.List;

import org.opendatakit.aggregate.odktables.entity.InternalRow;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Rows is a set of relations containing all the {@link InternalRow} entities for all
 * tables stored in the datastore. An instance of Rows contains all the Row
 * entities for a specific table.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Table extends TypedEntityRelation<InternalRow>
{
    /**
     * The name of the revisionTag field.
     */
    public static final String REVISION_TAG = "REVISION_TAG";

    /**
     * The revisionTag field.
     */
    private static final DataField revisionTag = new DataField(REVISION_TAG,
            DataType.STRING, false);
    
    /**
     * The namespace for Rows relations.
     */
    private static final String NAMESPACE = "ODKTABLES";

    private List<DataField> fields;

    /**
     * Constructs a Table. If the constructed Table does not already exist in
     * the datastore it will be created.
     * 
     * @param namespace
     *            the namespace the table should be created under.
     * @param aggregateTableIdentifier
     *            the globally unique identifier of the Table.
     * @param tableFields
     *            a list of DataFields representing the fields of the Table
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was a problem during communication with the
     *             datastore
     */
    private Table(String namespace, String aggregateTableIdentifier,
            List<DataField> tableFields, CallingContext cc)
            throws ODKDatastoreException
    {
        super(namespace, aggregateTableIdentifier, tableFields, cc);
        this.fields = tableFields;
    }

    @Override
    public InternalRow initialize(Entity entity) throws ODKDatastoreException
    {
        return new InternalRow(this, entity);
    }

    /**
     * @return a list of DataFields representing the columns of this table.
     */
    public List<DataField> getDataFields()
    {
        return Collections.unmodifiableList(this.fields);
    }

    public static Table getInstance(String aggregateTableIdentifier, CallingContext cc)
            throws ODKDatastoreException
    {
        List<DataField> tableFields = Columns.getInstance(cc).getDataFields(
                aggregateTableIdentifier);
        tableFields.add(revisionTag);
        return new Table(NAMESPACE, aggregateTableIdentifier, tableFields, cc);
    }
}