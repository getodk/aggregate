package org.opendatakit.aggregate.odktables.relation;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.entity.InternalFilter;
import org.opendatakit.common.ermodel.simple.Attribute;
import org.opendatakit.common.ermodel.simple.AttributeType;
import org.opendatakit.common.ermodel.simple.Entity;
import org.opendatakit.common.ermodel.simple.typedentity.TypedEntityRelation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * <p>
 * Filters is a relation containing all {@link InternalFilter} entities stored
 * in the datastore. Filters defines filters that users have set on their
 * synchronized tables.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public class Filters extends TypedEntityRelation<InternalFilter> {

    // Field names
    /**
     * The name of the aggregateUserIdentifier field.
     */
    public static String AGGREGATE_USER_IDENTIFIER = "AGGREGATE_USER_IDENTIFIER";

    /**
     * The name of the aggregateTableIdentifier field.
     */
    public static String AGGREGATE_TABLE_IDENTIFIER = "AGGREGATE_TABLE_IDENTIFIER";

    /**
     * The name of the columnName field.
     */
    public static String COLUMN_NAME = "COLUMN_NAME";

    /**
     * The name of the filterOperation field.
     */
    public static String FILTER_OPERATION = "FILTER_OPERATION";

    /**
     * The name of the value field.
     */
    public static String VALUE = "VALUE";

    // Relation name
    /**
     * The name of the Filters relation.
     */
    private static final String RELATION_NAME = "FILTERS";

    // The following defines the actual attributes that will be in the
    // datastore:
    /**
     * The aggregateTableIdentifier field.
     */
    private static final Attribute aggregateUserIdentifier = new Attribute(
	    AGGREGATE_USER_IDENTIFIER, AttributeType.STRING, false);

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
     * The filterOperation field.
     */
    private static final Attribute filterOperation = new Attribute(
	    FILTER_OPERATION, AttributeType.STRING, false);
    /**
     * The value field.
     */
    private static final Attribute value = new Attribute(VALUE,
	    AttributeType.STRING, false);

    private static final List<Attribute> attributes;
    static {
	attributes = new ArrayList<Attribute>();
	attributes.add(aggregateUserIdentifier);
	attributes.add(aggregateTableIdentifier);
	attributes.add(columnName);
	attributes.add(filterOperation);
	attributes.add(value);
    }

    /**
     * The singleton instance of the Filters relation.
     */
    private static Filters instance;

    /**
     * Constructs an instance which can be used to manipulate the Filters
     * relation. If the Filters relation does not already exist in the datastore
     * it will be created.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance
     * @throws ODKDatastoreException
     *             if there was a problem during communication with the
     *             datastore
     */
    private Filters(CallingContext cc) throws ODKDatastoreException {
	super(Table.NAMESPACE, RELATION_NAME, attributes, cc);
    }

    @Override
    public InternalFilter initialize(Entity entity)
	    throws ODKDatastoreException {
	return InternalFilter.fromEntity(entity);
    }

    /**
     * Returns the singleton instance of the Filters relation.
     * 
     * @param cc
     *            the CallingContext of this Aggregate instance. Must not be
     *            null.
     * @return the singleton instance of this Filters relation. If the instance
     *         does not exist or if the CallingContext has changed since it was
     *         constructed, then constructs and returns a new instance.
     * @throws ODKDatastoreException
     *             if there is a problem communicating with the datastore
     */
    public static Filters getInstance(CallingContext cc)
	    throws ODKDatastoreException {
	if (instance == null || instance.getCC() != cc) {
	    instance = new Filters(cc);
	}
	return instance;
    }
}
