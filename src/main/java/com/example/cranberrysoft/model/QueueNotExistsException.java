package com.example.cranberrysoft.model;

/**
 * The queue referred to does not exist.
 *
 * @author Mariusz Dubielecki
 */
public class QueueNotExistsException extends RuntimeException  {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new QueueDoesNotExistException with the specified error
     * message.
     *
     * @param message
     *        Describes the error encountered.
     */
    public QueueNotExistsException(String message) {
        super(message);
    }

}
