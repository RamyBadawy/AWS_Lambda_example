package com.gaggle.exceptions;

public class NoUserFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public NoUserFoundException(long id) {
	    super(String.format("User with id %d does not exist", id));
	  }
	
	public NoUserFoundException(String searchValue) {
	    super(String.format("User with letters %s in his first or last name does not exist", searchValue));
	  }
}
