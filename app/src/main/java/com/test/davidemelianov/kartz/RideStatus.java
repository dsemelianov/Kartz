package com.test.davidemelianov.kartz;

/**
 * Created by davidemelianov on 7/20/17.
 */

public enum RideStatus {
    AVAILABLE,
    CLAIMED,
    IN_TRANSIT,
    EXPIRED,
    CANCELLED_BY_PASSENGER,
    CANCELLED_BY_DRIVER,
    NEEDS_PAYMENT,
    PAYMENT_FAILED,
    NEEDS_REVIEW,
    COMPLETED;


    public int value() {
        switch (this) {
            case AVAILABLE:
                return 0;
            case CLAIMED:
                return 1;
            case IN_TRANSIT:
                return 2;
            case EXPIRED:
                return 3;
            case CANCELLED_BY_PASSENGER:
                return 4;
            case CANCELLED_BY_DRIVER:
                return 5;
            case NEEDS_PAYMENT:
                return 6;
            case PAYMENT_FAILED:
                return 7;
            case NEEDS_REVIEW:
                return 8;
            case COMPLETED:
                return 9;
            default:
                throw new AssertionError("Unknown state " + this);
        }
    }

}