package org.opendatakit.common.ermodel.simple;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

public class Data
{
    public String attrName;
    public String attrAge;

    public CallingContext cc;
    public String namespace;
    public String personRelationName;
    public List<Attribute> attributes;
    public Relation relation;

    public String johnsName;
    public int johnsAge;
    public String johnsIdentifier;

    public String joesName;
    public int joesAge;
    public String joesIdentifier;

    public Comparator<Attribute> attrComparator;

    public Data() throws ODKDatastoreException
    {
        attrName = "NAME";
        attrAge = "AGE";

        cc = TestContextFactory.getCallingContext();
        namespace = "MY_NAMESPACE";
        personRelationName = "PERSON";

        Attribute personName = new Attribute(attrName, AttributeType.STRING,
                false);
        Attribute personAge = new Attribute(attrAge, AttributeType.INTEGER,
                false);

        attributes = new ArrayList<Attribute>();
        attributes.add(personName);
        attributes.add(personAge);

        relation = new Relation(namespace, personRelationName, attributes, cc);

        johnsName = "John Doe";
        johnsAge = 50;

        Entity john = relation.newEntity();
        john.set(attrName, johnsName);
        john.set(attrAge, johnsAge);
        john.save();

        johnsIdentifier = john.getAggregateIdentifier();

        joesName = "Joe Doe";
        joesAge = 50;

        Entity joe = relation.newEntity();
        joe.set(attrName, joesName);
        joe.set(attrAge, joesAge);
        joe.save();

        joesIdentifier = joe.getAggregateIdentifier();

        attrComparator = new Comparator<Attribute>()
        {
            public int compare(Attribute o1, Attribute o2)
            {
                return o1.getName().compareTo(o2.getName());
            }

        };
    }
}
