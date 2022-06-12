package com.visma.task.exceptionHandler;

import com.visma.task.exceptions.DateException;
import com.visma.task.exceptions.MeetingException;
import com.visma.task.exceptions.UserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(DateException.class)
    public ResponseEntity<ExceptionDto> handleDateException(DateException ex) {
        System.out.println(ex.toString());
        return buildErrorEntity(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(MeetingException.class)
    public ResponseEntity<ExceptionDto> handleMeetingException(MeetingException ex) {
        System.out.println(ex.toString());
        return buildErrorEntity(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ExceptionDto> handleUserException(UserException ex) {
        System.out.println(ex.toString());
        return buildErrorEntity(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDto> handleGenericException(Exception ex) {
        return buildErrorEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<ExceptionDto> buildErrorEntity(HttpStatus httpStatus, Exception ex) {
        System.out.println(ex.toString());
        return new ResponseEntity<>(new ExceptionDto(httpStatus.toString(), ex.getMessage()), httpStatus);
    }

    private ResponseEntity<ExceptionDto> buildErrorEntity(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(new ExceptionDto(httpStatus.toString(), message), httpStatus);
    }

}