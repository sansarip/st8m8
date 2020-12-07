package com.sansarip.st8m8;

public class InvalidFSMException extends RuntimeException {
        public static final String INVALID_FSM_MESSAGE = "No valid FSM was found";
        public InvalidFSMException() {
            super(INVALID_FSM_MESSAGE);
        }
        public InvalidFSMException(String string) {
            super(string);
        }
}
