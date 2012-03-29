package org.opendatakit.aggregate.datamodel;


import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendatakit.aggregate.datamodel.FormDataModel.ElementType;

@RunWith(org.junit.runners.JUnit4.class)
public class FormElementModelTest {

	@Test
    public void testSimpleMappings() {
    	FormElementModel m1 = new FormElementModel(null, FormElementModel.Metadata.META_INSTANCE_ID);
    	FormElementModel m2 = new FormElementModel(null, FormElementModel.Metadata.META_MODEL_VERSION);
    	assertEquals(m1.equals(null), false);    	
    	assertEquals(m1.equals("fido"), false);    	
    	assertEquals(m1.equals(m2), false);    	
    	assertEquals(m2.equals(m1), false);
    	assertEquals(m1.equals(m1), true);
    	assertEquals(m2.equals(m2), true);
    }


	@Test
    public void testCrossMetaMappings() {
    	FormElementModel m1 = new FormElementModel(null, FormElementModel.Metadata.META_INSTANCE_ID);
    	FormDataModel fm = new FormDataModel("schema1");
    	fm.setElementType(ElementType.GROUP);
    	FormElementModel m2 = new FormElementModel(fm, null);
    	
    	assertEquals(m1.equals(m2), false);
    	assertEquals(m2.equals(m1), false);
    	assertEquals(m1.equals(m1), true);
    	assertEquals(m2.equals(m2), true);
    	assertEquals(m2.equals(null), false);
    	assertEquals(m2.equals("fido"), false);
    	
    	for ( FormElementModel cm : m2.getChildren()) {
    		assertEquals(m1.equals(cm), false);
    		assertEquals(m2.equals(cm), false);
    		assertEquals(cm.equals(m1), false);
    		assertEquals(cm.equals(m2), false);
    		assertEquals(cm.equals(null), false);
    		assertEquals(cm.equals("fido"), false);
    		assertEquals(cm.equals(cm), true);
    	}
    }
}
