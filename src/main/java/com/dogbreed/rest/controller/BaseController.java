package com.dogbreed.rest.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.dogbreed.Exception.NotFoundException;
import com.dogbreed.rest.response.ErrorResponse;

import org.springframework.http.HttpStatus;

public class BaseController {
	
	@ResponseBody
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ExceptionHandler(IllegalArgumentException.class)
	ErrorResponse handleException (IllegalArgumentException e) {
		return new com.dogbreed.rest.response.ErrorResponse(HttpStatus.BAD_REQUEST.toString(), "Missing/Invalid Argument");
	}
	
	@ResponseStatus(value = HttpStatus.NOT_FOUND)
	@ExceptionHandler(NotFoundException.class)
	ErrorResponse handleException (NotFoundException e) {
		return new ErrorResponse(HttpStatus.NOT_FOUND.toString(), "Requested Resource Not found");
	}

	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	ErrorResponse handleException (Exception e) {
		return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.toString(), "Someting went wrong");
	}
	
}
