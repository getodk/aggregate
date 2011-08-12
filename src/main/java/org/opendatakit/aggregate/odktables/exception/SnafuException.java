package org.opendatakit.aggregate.odktables.exception;

public class SnafuException extends RuntimeException
{
    private static final long serialVersionUID = -1572566812999179991L;

    public SnafuException(String message)
    {
        super(message);
    }

    public SnafuException(Throwable cause)
    {
        super(cause);
    }

    public SnafuException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
