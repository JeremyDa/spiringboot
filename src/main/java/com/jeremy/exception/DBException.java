package com.jeremy.exception;


public class DBException extends SystemException {

	private static final long serialVersionUID = 1L;

	public DBException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	} 

	public DBException(String errId, Object... args) {
		super(errId, args);
	}

	public DBException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public DBException(String errId, Throwable cause, Object... args) {
		super(errId, cause, args);
	}

}
