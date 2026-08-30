package dev.pawan.rupixo.common.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends RuntimeException {

    private final long resetTimeMillis;

    public RateLimitExceededException(long resetTimeMillis, String message) {
        super(message + " Please try again after " + resetTimeMillis + " milliseconds.");
        this.resetTimeMillis = resetTimeMillis;
    }
}
