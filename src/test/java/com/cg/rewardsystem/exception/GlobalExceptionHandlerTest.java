package com.cg.rewardsystem.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleNoSuchElementException() {
        NoSuchElementException ex = new NoSuchElementException("Not found");
        Map<String, String> response = handler.handleNoSuchElementExcepition(ex);
        assertEquals("Resource not found.", response.get("error"));
        assertEquals(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.value());
    }

    @Test
    void testHandleGeneralException() {
        Exception ex = new Exception("Something went wrong");
        Map<String, String> response = handler.handelExcepition(ex);
        assertEquals("An unexpected error occurred.", response.get("error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void testHandleDataNotFoundException() {
        DataNotFoundException ex = new DataNotFoundException("Customer data missing");
        Map<String, String> response = handler.handelDataNotFound(ex);
        assertEquals("Customer data missing", response.get("error"));
    }
}