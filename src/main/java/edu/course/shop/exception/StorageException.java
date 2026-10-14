package edu.course.shop.exception;
public final class StorageException extends RuntimeException {
    public StorageException(Throwable cause) { super("The database could not complete this operation. Please try again.", cause); }
}