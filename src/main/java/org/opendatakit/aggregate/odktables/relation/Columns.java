package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entity.InternalColumn;
import org.opendatakit.common.ermodel.simple.Attribute;
import org.opendatakit.common.ermodel.simple.AttributeType;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Columns is a relation containing all {@link InternalColumn} entities stored
 * in the datastore. Columns defines the columns for all tables. That is, the
 * set of all Columns entities which have the same aggregateTableIdentifier
 * serves as the definition for the columns of that table.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Columns extends TypedEntityRelation<InternalColumn> {
    // Field names
    /**
     * The name of the aggregateTableIdentifier field.
     */
    public static String AGGREGATE_TABLE_IDENTIFIER = "AGGREGATE_TABLE_IDENTIFIER";

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

    // The following defines the actual attributes that will be in the
    // datastore:
    /**
     * The aggregateTableIdentifier field.
     */
    private static final Attribute aggregateTableIdentifier = new Attribute(
	    AGGREGATE_TABLE_IDENTIFIER, AttributeType.STRING, false);
    /**
     * The columnName field.
     */
    private static final Attribute columnName = new Attribute(COLUMN_NAME,
	    AttributeType.STRING, false);
    /**
     * The columnType field.
     */
    private static final Attribute columnType = new Attribute(COLUMN_TYPE,
	    AttributeType.STRING, false);
    /**
     * The nullable field.
     */
    private static final Attribute nullable = new Attribute(NULLABLE,
	    AttributeType.BOOLEAN, false);

    private static final List<Attribute> attributes;
    static {
	attributes = new ArrayList<Attribute>();
	attributes.add(aggregateTableIdentifier);
	attributes.add(columnName);
	attributes.add(columnType);
	attributes.add(nullable);
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
    private Columns(CallingContext cc) throws ODKDatastoreException {
	super(Table.NAMESPACE, RELATION_NAME, attributes, cc);
    }

    @Override
    public InternalColumn initialize(Entity entity)
	    throws ODKDatastoreException {
	return InternalColumn.fromEntity(entity);
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
	    throws ODKDatastoreException {
	if (instance == null || instance.getCC() != cc) {
	    instance = new Columns(cc);
	}
	return instance;
    }
}
