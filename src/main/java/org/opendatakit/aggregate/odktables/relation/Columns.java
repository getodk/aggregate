package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entity.Column;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Columns is a relation containing all {@link Column} entities stored in the
 * datastore. Columns defines the columns for all tables. That is, the set of
 * all Columns entities which have the same tableUUID serves as the definition
 * for the columns of that table.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Columns extends TypedEntityRelation<Column>
{
    // Field names
    /**
     * The name of the tableUUID field.
     */
    public static String TABLE_UUID = "TABLE_UUID";

    /**
     * The name of the columnName field.
     */
    public static String COLUMN_NAME = "COLUMN_NAME";

    /**
     * The name of the columnType field.
     */
    public static String COLUMN_TYPE = "COLUMN_TYPE";

    /**
     * The name of the nullable field.
     */
    public static String NULLABLE = "NULLABLE";

    // Relation name
    /**
     * The name of the Columns relation.
     */
    private static final String RELATION_NAME = "COLUMNS";

    // The following defines the actual fields that will be in the datastore:
    /**
     * The tableUUID field.
     */
    private static final DataField tableUUID = new DataField(TABLE_UUID,
            DataType.URI, false);
    /**
     * The columnName field.
     */
    private static final DataField columnName = new DataField(COLUMN_NAME,
            DataType.STRING, false);
    /**
     * The columnType field.
     */
    private static final DataField columnType = new DataField(COLUMN_TYPE,
            DataType.STRING, false);
    /**
     * The nullable field.
     */
    private static final DataField nullable = new DataField(NULLABLE,
            DataType.BOOLEAN, false);

    private static final List<DataField> fields;
    static
    {
        fields = new ArrayList<DataField>();
        fields.add(tableUUID);
        fields.add(columnName);
        fields.add(columnType);
        fields.add(nullable);
    }
    /**
     * The singleton instance of the Columns relation.
     */
    private static Columns instance;

    /**
     * Constructs an instance which can be used to manipulate the Columns
     * relation. If the Columns relation does not already exist in the datastore
     * it will be created.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was a problem during communication with the
     *             datastore
     */
    private Columns(CallingContext cc) throws ODKDatastoreException
    {
        super(RELATION_NAME, fields, cc);
    }

    public List<DataField> getDataFields(String tableUUID)
            throws ODKDatastoreException
    {
        List<Column> columns = query().equal(TABLE_UUID, tableUUID).execute();
        List<DataField> fields = new ArrayList<DataField>();
        for (Column column : columns)
        {
            DataField field = column.toDataField();
            fields.add(field);
        }
        return fields;
    }

    @Override
    public Column initialize(Entity entity) throws ODKDatastoreException
    {
        return new Column(entity, getCC());
    }

    /**
     * Returns the singleton instance of the Columns relation.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance. Must not be
     *            null.
     * @return the singleton instance of this Columns relation. If the instance
     *         does not exist or if the CallingContext has changed since it was
     *         constructed, then constructs and returns a new instance.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     */
    public static Columns getInstance(CallingContext cc)
            throws ODKDatastoreException
    {
        if (instance == null || instance.getCC() != cc)
        {
            instance = new Columns(cc);
        }
        return instance;
    }
}
