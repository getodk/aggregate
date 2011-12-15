package org.opendatakit.aggregate.odktables.client.exception;

public class Http404Exception extends RuntimeException {
    private static final long serialVersionUID = -3382934780226508812L;

    public Http404Exception(String message) {
	super(message);
    }

}
