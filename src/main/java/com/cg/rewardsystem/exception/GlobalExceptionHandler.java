package com.cg.rewardsystem.exception;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

		/**
		 * Handle resource not found exception.
		 * 
		 */
		@ExceptionHandler(NoSuchElementException.class) 
		@ResponseStatus (HttpStatus.NOT_FOUND)
		@ResponseBody
		public Map<String, String> handleNoSuchElementExcepition(NoSuchElementException ex) { 
			return Collections.singletonMap("error", "Resource not found.");
		}

		/**
		 * Handle general exception.
		 * 
		 */
		@ExceptionHandler(Exception.class) 
		@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
		@ResponseBody
		public Map<String, String> handelExcepition(Exception ex) { 
			return Collections.singletonMap("error", "An unexpected error occurred.");
		}

		/**
		 * Handle if any error occurred
		 * 
		 */
		@ExceptionHandler(DataNotFoundException.class)
		public Map<String, String> handelDataNotFound(DataNotFoundException ex){
			return Collections.singletonMap("error", ex.getMessage());
		}

		
}