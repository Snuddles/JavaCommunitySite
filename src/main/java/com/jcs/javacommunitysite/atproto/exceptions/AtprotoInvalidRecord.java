package com.jcs.javacommunitysite.atproto.exceptions;

public class AtprotoInvalidRecord extends RuntimeException {
    public AtprotoInvalidRecord(String message) {
        super(message);
    }
    public AtprotoInvalidRecord() { super(); }
}
