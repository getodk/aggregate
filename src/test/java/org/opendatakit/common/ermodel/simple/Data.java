package org.opendatakit.common.ermodel.simple;

import java.util.ArrayList;
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
        john.setString(attrName, johnsName);
        john.setInteger(attrAge, johnsAge);
        john.save();

        johnsIdentifier = john.getAggregateIdentifier();
        
        joesName = "Joe Doe";
        joesAge = 50;

        Entity joe = relation.newEntity();
        joe.setString(attrName, joesName);
        joe.setInteger(attrAge, joesAge);
        joe.save();

        joesIdentifier = joe.getAggregateIdentifier();
    }
}
