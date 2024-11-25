package com.ces.aquarium.exceptions;

public class EntityException extends RuntimeException {

	private static final long serialVersionUID = 4240546934827596019L;
	public EntityException() {
        super();
    }
    public EntityException(String message, Throwable cause) {
        super(message, cause);
    }
    public EntityException(String message) {
        super(message);
    }
    public EntityException(Throwable cause) {
        super(cause);
    }
	
}
