package com.visma.task.exceptionHandler;

import lombok.NonNull;
import lombok.Value;

@Value
public class ExceptionDto {
    @NonNull
    String errorCode;

    @NonNull
    String message;
}
