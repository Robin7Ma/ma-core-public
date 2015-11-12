/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.web.mvc.rest.v1.exception;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import com.serotonin.m2m2.web.mvc.rest.v1.model.RestErrorModel;

/**
 * 
 * Class to handle REST Specific Errors and present the user with a Model
 * and also log the errors neatly in our logs
 * 
 * @author Terry Packer
 *
 */
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
	 protected Log LOG = LogFactory.getLog(RestExceptionHandler.class);
	
    @ExceptionHandler({ 
    	NoSupportingModelException.class,
    	ModelNotFoundException.class,
    	Exception.class,
    	RuntimeException.class
    	})
    protected ResponseEntity<Object> handleMangoError(Exception e, WebRequest request) {
    	
    	//Log this
    	LOG.error(e.getMessage(), e);
    	
    	RestErrorModel error = new RestErrorModel(e);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Messages", "error");
        headers.set("Errors", e.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
    	return handleExceptionInternal(e, error, headers, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler#handleExceptionInternal(java.lang.Exception, java.lang.Object, org.springframework.http.HttpHeaders, org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
    		Object body, HttpHeaders headers, HttpStatus status,
    		WebRequest request) {
    	LOG.error(ex.getMessage(), ex);
    	RestErrorModel error = new RestErrorModel(ex);
        headers.set("Messages", "error");
        headers.set("Errors", ex.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
        if(body == null)
        	body = error;
    	return super.handleExceptionInternal(ex, body, headers, status, request);
    }

	@Override
	protected ResponseEntity<Object> handleNoSuchRequestHandlingMethod(
			NoSuchRequestHandlingMethodException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
    	LOG.error(ex.getMessage(), ex);
        headers.set("Messages", "error");
        headers.set("Errors", ex.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
		return super.handleNoSuchRequestHandlingMethod(ex, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
			HttpRequestMethodNotSupportedException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
    	LOG.error(ex.getMessage(), ex);
        headers.set("Messages", "error");
        headers.set("Errors", ex.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
		return super.handleHttpRequestMethodNotSupported(ex, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
			HttpMediaTypeNotSupportedException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
    	LOG.error(ex.getMessage(), ex);
        headers.set("Messages", "error");
        headers.set("Errors", ex.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
		return super.handleHttpMediaTypeNotSupported(ex, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
			HttpMediaTypeNotAcceptableException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
    	LOG.error(ex.getMessage(), ex);
        headers.set("Messages", "error");
        headers.set("Errors", ex.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
		return super.handleHttpMediaTypeNotAcceptable(ex, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(
			MissingServletRequestParameterException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
    	LOG.error(ex.getMessage(), ex);
        headers.set("Messages", "error");
        headers.set("Errors", ex.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
		return super.handleMissingServletRequestParameter(ex, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleServletRequestBindingException(
			ServletRequestBindingException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
    	LOG.error(ex.getMessage(), ex);
        headers.set("Messages", "error");
        headers.set("Errors", ex.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
		return super.handleServletRequestBindingException(ex, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleConversionNotSupported(
			ConversionNotSupportedException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
    	LOG.error(ex.getMessage(), ex);
        headers.set("Messages", "error");
        headers.set("Errors", ex.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
		return super.handleConversionNotSupported(ex, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleTypeMismatch(
			TypeMismatchException ex, HttpHeaders headers, HttpStatus status,
			WebRequest request) {
    	LOG.error(ex.getMessage(), ex);
        headers.set("Messages", "error");
        headers.set("Errors", ex.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
		return super.handleTypeMismatch(ex, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
			HttpMessageNotReadableException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
    	LOG.error(ex.getMessage(), ex);
        headers.set("Messages", "error");
        headers.set("Errors", ex.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
		return super.handleHttpMessageNotReadable(ex, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotWritable(
			HttpMessageNotWritableException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
    	LOG.error(ex.getMessage(), ex);
        headers.set("Messages", "error");
        headers.set("Errors", ex.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
		return super.handleHttpMessageNotWritable(ex, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
    	LOG.error(ex.getMessage(), ex);
        headers.set("Messages", "error");
        headers.set("Errors", ex.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
		return super.handleMethodArgumentNotValid(ex, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestPart(
			MissingServletRequestPartException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
    	LOG.error(ex.getMessage(), ex);
        headers.set("Messages", "error");
        headers.set("Errors", ex.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
		return super.handleMissingServletRequestPart(ex, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleBindException(BindException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
    	LOG.error(ex.getMessage(), ex);
        headers.set("Messages", "error");
        headers.set("Errors", ex.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
		return super.handleBindException(ex, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(
			NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status,
			WebRequest request) {
    	LOG.error(ex.getMessage(), ex);
        headers.set("Messages", "error");
        headers.set("Errors", ex.getMessage());
        headers.setContentType(MediaType.APPLICATION_JSON);
		return super.handleNoHandlerFoundException(ex, headers, status, request);
	}
    
    
}
