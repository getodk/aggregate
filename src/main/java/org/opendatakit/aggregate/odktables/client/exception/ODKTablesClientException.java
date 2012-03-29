package org.opendatakit.aggregate.odktables.client.exception;

/**
 * <p>
 * Base class for all exceptions thrown by odktables client code. Subclass this
 * if you need to provide an exception on the client side for a failure case of
 * a command.
 * </p>
 * 
 * <p>
 * Subclasses should have a descriptive name which clearly describes the error.
 * They should provide a single constructor which takes all the necessary data
 * for a client to figure out exactly what went wrong, and provide getter
 * methods for this data. Also, make sure to call the super constructor, passing
 * it the desired error message (retrieved by {@link Exception#getMessage()}.
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public abstract class ODKTablesClientException extends Exception {
    /**
     * Serial number for serialization
     */
    private static final long serialVersionUID = -2320458565807877689L;

    /**
     * Constructs a new ODKTablesClientException.
     * 
     * @param message
     *            the message for the exception
     */
    protected ODKTablesClientException(String message) {
	super(message);
    }
}
