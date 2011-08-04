/*
   NOTE: this JUnit 4 runner is originally from the Migen project,
   http://code.google.com/p/migen/. It has been ported from JUnit 4.5
   to JUnit 4.8.2 by Brian Burg <burg@cs.washington.edu>.
*/

/*
 * (c) 2011, University of Washington.
   (c) 2010, The Migen developers.

  Migen is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

//EDIT: BJB: changed package to 331-specific package. Normally,
//      you should not change the package but it would cause a
//      lot of grief for the course staffs.

//package uk.ac.lkl.common.util.testing;
package org.opendatakit.aggregate.odktables.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;

public class LabelledParameterized extends Parameterized {

    @Retention(value = RetentionPolicy.RUNTIME)
	@Target(value = ElementType.METHOD)
	public static @interface Labels {
	    // no args at present
	}

    private List<String> labels;

    private Description labelledDescription;

    public LabelledParameterized(Class<?> cl) throws Throwable {
        super(cl);
        initialiseLabels();
        generateLabelledDescription();
    }

    //necessary to make assignment at the end work.
    @SuppressWarnings("unchecked")
	private void initialiseLabels() throws Exception {
        List<FrameworkMethod> methods = this.getTestClass().getAnnotatedMethods(Labels.class);

        Method foundLabelMethod = null;
        for (FrameworkMethod junitMethod : methods) {
        	Method method = junitMethod.getMethod();
        	
            if (foundLabelMethod != null)
                throw new Exception(
				    "Only one method should be annotated with @Labels");

            int modifiers = method.getModifiers();
            if (!Modifier.isStatic(modifiers))
                throw new Exception("@Labels method must be static");

            Class<?> returnType = method.getReturnType();
            if (returnType != List.class)
                throw new Exception(
				    "Return type of @Labels method must be List<String>");

            Type type = method.getGenericReturnType();
            if (!(type instanceof ParameterizedType))
                throw new Exception(
				    "Return type of @Labels method must be List<String>");

            ParameterizedType genericType = (ParameterizedType) type;
            Type[] typeArguments = genericType.getActualTypeArguments();

            if (typeArguments[0] != String.class)
                throw new Exception(
				    "Return type of @Labels method must be List<String>");

            foundLabelMethod = method;
        }
        if (foundLabelMethod == null)
            throw new Exception("No @Labels method found");
        
        this.labels = (List<String>) foundLabelMethod.invoke(null);
    }

    /**
     * Relies on JUnit 4.4. Can't use childlessCopy() method that features in
     * later versions.
     * 
     * Relies on childDescriptions list being modifiable.
     * 
     */
    private void generateLabelledDescription() throws Exception {
        Description originalDescription = super.getDescription();
        labelledDescription = Description
	    .createSuiteDescription(originalDescription.getDisplayName());
        ArrayList<Description> childDescriptions = originalDescription
	    .getChildren();
        int childCount = childDescriptions.size();
        if (childCount != labels.size())
            throw new Exception(
				"Number of labels and number of parameters must match.");

        for (int i = 0; i < childDescriptions.size(); i++) {
            Description childDescription = childDescriptions.get(i);
            String label = labels.get(i);
            Description newDescription = Description
		.createSuiteDescription(label);
            ArrayList<Description> grandChildren = childDescription
		.getChildren();
            for (Description grandChild : grandChildren)
                newDescription.addChild(grandChild);
            labelledDescription.addChild(newDescription);
        }
    }

    @Override
	public Description getDescription() {
        return labelledDescription;
    }

}