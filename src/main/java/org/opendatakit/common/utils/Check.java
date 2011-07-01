package org.opendatakit.common.utils;

/**
 * Class for checking preconditions on method arguments.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public final class Check
{

    /**
     * @param variable
     *            the variable to check
     * @param name
     *            the name of the variable
     * @throws IllegalArgumentException
     *             if variable is null, throws an IllegalArgumentException with
     *             the message "name was null", where name is the variable you
     *             pass in for name.
     */
    public static void notNull(Object variable, String name)
    {
        if (variable == null)
            throw new IllegalArgumentException(String.format("%s was null",
                    name));
    }

    /**
     * @param variable
     *            the variable to check
     * @param name
     *            the name of the variable
     * @throws IllegalArgumentException
     *             if variable is null or empty
     */
    public static void notNullOrEmpty(String variable, String name)
    {
        Check.notNull(variable, name);
        if (variable.length() == 0)
            throw new IllegalArgumentException(String.format("%s was empty",
                    name));

    }
}
