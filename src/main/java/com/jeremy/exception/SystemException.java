package com.jeremy.exception;
 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public SystemException(String msg) {
		logger.error(msg);
	}

	public SystemException(String errId, Object... args) {
		logger.error(errId+":"+ args.toString());
	}
	
	public SystemException(String msg, Throwable t) {
		logger.error(msg,t);
	}

	public void setExpression(String excSource) {
		// TODO Auto-generated method stub
		
	}
	
	
}

