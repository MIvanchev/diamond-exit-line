package com.podrug.line.util;

/**
 * TODO
 */
public class IllegalOperationException extends RuntimeException {

    /**
     * TODO
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    public IllegalOperationException()
    {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public IllegalOperationException(String message)
    {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public IllegalOperationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     */
    public IllegalOperationException(Throwable cause)
    {
        super(cause);
    }
}