package com.booking.customexception;
public class InsufficientSeatException extends RuntimeException {
    private static final long serialVersionUID = 1L; public InsufficientSeatException(String m){super(m);} }
