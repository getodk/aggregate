package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendatakit.common.ermodel.AbstractRelationAdapter;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.ermodel.Relation;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * TableIndex is a set of (userId, tableId, tableName, tableFields) tuples, aka
 * 'entities' where
 * <ul>
 * <li>userId = the unique identifier of the user who owns the table represented
 * by this row</li>
 * <li>tableId = the unique identifier of a specific Table in the datastore. All
 * tableIds must consist of only uppercase letters, numbers, and underscores and
 * must start with an uppercase letter.</li>
 * <li>tableName = the human readable name of the Table</li>
 * <li>tableFields = a set of fieldName:fieldType key-value pairs which define
 * the fields of the Table</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Thus TableIndex is an index of all the Table stored in the datastore.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class TableIndex extends AbstractRelationAdapter
{
    // Field names:
    /**
     * The name of the user id field.
     */
    public static final String USER_ID = "USER_ID";
    /**
     * The name of the table id field.
     */
    public static final String TABLE_ID = "TABLE_ID";
    /**
     * The name of the field for the human readable name of a table.
     */
    public static final String TABLE_NAME = "TABLE_NAME";
    /**
     * <p>
     * The name of the field which stores a serialized form of a table's fields.
     * Usage:
     * </p>
     * 
     * <pre>
     * List&lt;DataField&gt; fields;
     * // ... add DataFields ...
     * entity.setField(TableIndex.TABLE_FIELDS, TableIndex.serializeFields(fields));
     * </pre>
     */
    public static final String TABLE_FIELDS = "TABLE_FIELDS";

    // Relation name and Table namespace:
    /**
     * The namespace for Table relations.
     */
    public static final String TABLE_NAMESPACE = "ODKTABLES";
    /**
     * The name of the TableIndex relation.
     */
    private static final String RELATION_NAME = "TABLE_INDEX";

    // The following defines the actual fields that will be in the datastore:
    /**
     * The field for the user id.
     */
    private static final DataField userId = new DataField(USER_ID,
            DataType.STRING, false);
    /**
     * The field for the table id.
     */
    private static final DataField tableId = new DataField(TABLE_ID,
            DataType.STRING, false);
    /**
     * The field for the human readable name of a table.
     */
    private static final DataField tableName = new DataField(TABLE_NAME,
            DataType.STRING, false);
    /**
     * The field for the definition of a table's fields.
     */
    private static final DataField tableFields = new DataField(TABLE_FIELDS,
            DataType.STRING, false);

    private static final List<DataField> fields;
    static
    {
        userId.setIndexable(IndexType.HASH);
        tableId.setIndexable(IndexType.HASH);

        fields = new ArrayList<DataField>();
        fields.add(userId);
        fields.add(tableId);
        fields.add(tableName);
        fields.add(tableFields);
    }
    /**
     * The regex pattern used to deserialize a list of DataFields when it comes
     * out of the datastore.
     */
    private static final Pattern SERIALIZED_REGEX = Pattern
            .compile("\\{(([A-Z_0-9]+:[A-Z_]+,)+)\\}");

    /**
     * The singleton instance of the TableIndex.
     */
    private static TableIndex instance;
    /**
     * The CallingContext of the Aggregate instance.
     */
    private static CallingContext cc;

    /**
     * Constructs an instance which can be used to manipulate the TableIndex
     * relation. If the TableIndex relation does not already exist in the
     * datastore it will be created.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was a problem during communication with the
     *             datastore
     */
    private TableIndex(CallingContext cc) throws ODKDatastoreException
    {
        super(RELATION_NAME, fields, cc);
        TableIndex.cc = cc;
    }

    /**
     * Creates a Table with the given userId, tableId, tableName, and fields.
     * 
     * @param userId
     *            the unique identifier of the user who will own the table. This
     *            user must exist in the Users table. Must be non-null and
     *            non-empty.
     * @param tableId
     *            the unique identifier for the table. It must consist of only
     *            uppercase letters, numbers, and underscores and must start
     *            with an uppercase letter. There must not be another table with
     *            this tableId. Must be non-null and non-empty.
     * @param tableName
     *            the human readable name for the table. The tableName does not
     *            have to be unique. Must be non-null and non-empty.
     * @param fields
     *            a list of DataFields defining the fields the new Table will
     *            have. Must be non-null and non-empty.
     * @throws ODKDatastoreException
     *             if there was a problem communicating with the datastore
     * @throws IllegalArgumentException
     *             if any arguments are null or empty of if the tableId is badly
     *             formed
     * @throws RuntimeException
     *             if a Table with the given userId and tableId already exists
     */
    public void createTable(String userId, String tableId, String tableName,
            List<DataField> fields) throws ODKDatastoreException
    {
        if (userId == null || userId.isEmpty() || tableId == null
                || tableId.isEmpty() || tableName == null
                || tableName.isEmpty() || fields == null || fields.isEmpty())
        {
            throw new IllegalArgumentException(
                    "received null or empty argument");
        }
        if (!tableId.matches(Relation.VALID_UPPER_CASE_NAME_REGEX))
        {
            throw new IllegalArgumentException("badly formed tableId '"
                    + tableId + "'. Check that it consists of only uppercase "
                    + "letters, numbers, and underscores and that "
                    + "it starts with an uppercase letter.");
        }
        if (!Users.getInstance(cc).userExists(userId))
        {
            throw new IllegalArgumentException("No user with userId '" + userId
                    + "' exists!");
        }

        Entity indexEntry = retrieveEntity(userId, tableId);
        if (indexEntry != null)
        {
            throw new RuntimeException("Table with userId '" + userId
                    + "' and tableId: '" + tableId + "' already exists!");
        }

        // Add entry to index
        Entity entry = newEntity(cc);
        entry.setField(USER_ID, userId);
        entry.setField(TABLE_ID, tableId);
        entry.setField(TABLE_NAME, tableName);
        entry.setField(TABLE_FIELDS, serializeFields(fields));
        entry.persist(cc);
        // We can delay creation of table until it is actually needed
    }

    /**
     * Retrieves the Table with the given userId and tableId. This table must
     * previously have been created through {@link #createTable}.
     * 
     * @param userId
     *            the unique identifier of the user who owns the table. Must be
     *            non-null and non-empty and a user with this userId must exist
     *            in the Users table.
     * @param tableId
     *            the unique identifier of the table to retrieve. Must be
     *            non-null and non-empty and a table with this tableId must
     *            exist in the TableIndex.
     * @return the Table associated with the given tableId
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     * @throws IllegalArgumentException
     *             if no Table with the given tableId exists
     */
    public Table getTable(String userId, String tableId)
            throws ODKDatastoreException
    {
        Entity indexEntry = retrieveEntity(userId, tableId);
        if (indexEntry == null)
        {
            throw new IllegalArgumentException("No table with userId '"
                    + userId + "' and tableId '" + tableId + "' exists!");
        }
        List<DataField> deserializedFields = deserializeFields(indexEntry
                .getField(TABLE_FIELDS));
        Table table = new Table(TABLE_NAMESPACE, createTableName(userId,
                tableId), deserializedFields, cc);
        return table;
    }

    /**
     * @param userId
     *            the unique identifier of the user who owns the table. Must be
     *            non-null and non-empty and a user with this userId must exist
     *            in the Users table.
     * @param tableId
     *            the unique identifier of the table to test for. Must be
     *            non-null and non-empty.
     * @return true if a Table with the given tableId exists in the datastore,
     *         false otherwise.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     */
    public boolean tableExists(String userId, String tableId)
            throws ODKDatastoreException
    {
        return retrieveEntity(userId, tableId) != null;
    }

    /**
     * Deletes the Table with the given userId and tableId. This table must
     * previously have been created through {@link #createTable}. Calling this
     * method is preferable to {@link Table#dropRelation(CallingContext)}
     * because that method will not delete the entry from the TableIndex.
     * 
     * @param userId
     *            the unique identifier of the user who owns the table. Must be
     *            non-null and non-empty and a user with this userId must exist
     *            in the Users table.
     * @param tableId
     *            the unique identifier of the table to delete. Must be non-null
     *            and non-empty. If no table with this tableId exists, then
     *            calling this method does nothing.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     */
    public void deleteTable(String userId, String tableId)
            throws ODKDatastoreException
    {
        Entity indexEntry = retrieveEntity(userId, tableId);
        if (indexEntry == null)
            return;
        List<DataField> deserializedFields = deserializeFields(indexEntry
                .getField(TABLE_FIELDS));
        Table table = new Table(TABLE_NAMESPACE, createTableName(userId,
                tableId), deserializedFields, cc);
        table.dropRelation(cc);
        indexEntry.remove(cc);
    }

    /**
     * Retrieves the entity with the given userId and tableId from the
     * datastore.
     * 
     * @param userId
     *            the value of the userId field of the desired Entity.
     * @param tableId
     *            the value of the tableId field of the desired Entity.
     * @return the Entity with the given tableId, or null if no such Entity
     *         exists.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore.
     */
    private Entity retrieveEntity(String userId, String tableId)
            throws ODKDatastoreException
    {
        if (userId == null || userId.isEmpty() || tableId == null
                || tableId.isEmpty())
        {
            throw new IllegalArgumentException(
                    "received null or empty argument");
        }

        Query q = createQuery(cc);
        q.addFilter(getDataField(USER_ID), FilterOperation.EQUAL, userId);
        q.addFilter(getDataField(TABLE_ID), FilterOperation.EQUAL, tableId);
        List<Entity> entities = executeQuery(q);

        if (entities == null || entities.isEmpty())
        {
            return null;
        }
        if (entities.size() > 1)
        {
            throw new RuntimeException(
                    "More than one entry for the given userId '" + userId
                            + "' and tableId '" + tableId + "'");
        }

        Entity entity = entities.get(0);
        return entity;
    }

    /**
     * Returns the singleton instance of the TableIndex.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance. Must not be
     *            null.
     * @return the singleton instance of this TableIndex. If the instance does
     *         not exist or if the CallingContext has changed since it was
     *         constructed, then constructs and returns a new instance.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     */
    public static TableIndex getInstance(CallingContext cc)
            throws ODKDatastoreException
    {
        if (instance == null || TableIndex.cc != cc)
        {
            instance = new TableIndex(cc);
        }
        return instance;
    }

    /**
     * Converts the given list of DataFields into a string. This string can be
     * converted back into a list using {@link #deserializeFields(String)}.
     * 
     * @param fields
     *            a list of fields to serialize. Can not be null but may be
     *            empty.
     * @return a string of the form "{name:dataType,name:dataType,...}" where
     *         each name:dataType pair represents one field in fields. 'name' is
     *         the name of the field while 'dataType' is the DataField.DataType
     *         of the field. Note that there will be a trailing comma on the end
     *         of the list. If fields is empty, returns "{}".
     */
    protected static String serializeFields(List<DataField> fields)
    {
        if (fields == null)
            throw new IllegalArgumentException(
                    "received null argument for fields");

        StringBuilder serialFields = new StringBuilder("{");
        for (DataField field : fields)
        {
            serialFields.append(field.getName());
            serialFields.append(":");
            serialFields.append(field.getDataType().name());
            serialFields.append(",");
        }
        serialFields.append("}");
        return serialFields.toString();
    }

    /**
     * Converts the given string into a list of DataFields. This string must be
     * of the form described in {@link #serializeFields(List)}.
     * 
     * @param fields
     *            the list of fields to deserialize. Must be of the form
     *            "{name:dataType,name:dataType,...}" as described in
     *            {@link #serializeFields(List)}. May not be null or empty, but
     *            may be "{}" to represent the empty list.
     * @return a list of DataFields
     */
    protected static List<DataField> deserializeFields(String fields)
    {
        if (fields == null || fields.isEmpty())
            throw new IllegalArgumentException(
                    "received null or empty argument for fields");

        List<DataField> fieldList = new ArrayList<DataField>();
        try
        {
            Matcher matcher = SERIALIZED_REGEX.matcher(fields);

            if (matcher.matches())
            {
                String serialFields = matcher.group(1);
                String[] fieldPairs = serialFields.split(",");
                for (String fieldPair : fieldPairs)
                {
                    String[] nameTypePair = fieldPair.split(":");
                    DataField field = new DataField(nameTypePair[0],
                            DataType.valueOf(nameTypePair[1]), false);
                    fieldList.add(field);
                }
            }
        } catch (IndexOutOfBoundsException e)
        {
            throw new IllegalArgumentException(
                    "bad input format for argument fields");
        } catch (NullPointerException e)
        {
            throw new IllegalArgumentException(
                    "bad input format for argument fields");
        }
        return fieldList;
    }

    /**
     * Creates a unique name which can be used to name a Table in the datastore.
     * This is for passing to the Table constructor.
     * 
     * @param userId
     *            the userId field of the table
     * @param tableId
     *            the tableId field of the table
     * @return a name which can be used to name the table
     */
    protected static String createTableName(String userId, String tableId)
    {
        return String.format("%s_%s", userId, tableId);
    }
}