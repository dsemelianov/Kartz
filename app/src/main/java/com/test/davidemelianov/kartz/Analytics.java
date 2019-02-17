package com.test.davidemelianov.kartz;

/**
 * Created by davidemelianov on 7/20/17.
 */

public class Analytics {

    public enum Events {
        SIGN_OUT,
        ENTER_NUMBER,
        RESEND_CODE,
        SIGN_IN,
        ENTER_EMAIL,
        SKIP_EMAIL,
        ENTER_NAME,
        SET_PASSENGERS,
        SET_LOCATION,
        OUTSIDE_OF_RANGE,
        REQUIRE_PAYMENT,
        CANCELLED_BY_PASSENGER,
        CANCELLED_BY_DRIVER,
        CLAIM_RIDE,
        CLAIM_STOLEN,
        CARD_ADDED;

        public String value() {
            switch (this) {
                case SIGN_OUT:
                    return "sign_out";
                case ENTER_NUMBER:
                    return "enter_number";
                case RESEND_CODE:
                    return "resend_code";
                case SIGN_IN:
                    return "sign_in";
                case ENTER_EMAIL:
                    return "enter_email";
                case SKIP_EMAIL:
                    return "skip_email";
                case ENTER_NAME:
                    return "enter_name";
                case SET_PASSENGERS:
                    return "set_passengers";
                case SET_LOCATION:
                    return "set_location";
                case OUTSIDE_OF_RANGE:
                    return "outside_of_range";
                case REQUIRE_PAYMENT:
                    return "require_payment";
                case CANCELLED_BY_PASSENGER:
                    return "cancelled_by_passenger";
                case CANCELLED_BY_DRIVER:
                    return "cancelled_by_driver";
                case CLAIM_RIDE:
                    return "claim_ride";
                case CLAIM_STOLEN:
                    return "claim_stolen";
                case CARD_ADDED:
                    return "card_added";
                default:
                    throw new AssertionError("Unknown state " + this);
            }
        }
    }

    public enum Parameters {
        ACTIVITY_NAME;

        public String value() {
            switch (this) {
                case ACTIVITY_NAME:
                    return "activity_name";
                default:
                    throw new AssertionError("Unknown state " + this);
            }
        }
    }

    public enum Activities {
        DRIVER_GIVE_RIDE,
        DRIVER_CONFIRM_RIDE_COMPLETE,
        PASSENGER_RIDE_ACCEPTED,
        PASSENGER_RIDE_SENT;

        public String value() {
            switch (this) {
                case DRIVER_GIVE_RIDE:
                    return "driver_give_ride";
                case DRIVER_CONFIRM_RIDE_COMPLETE:
                    return "driver_confirm_ride_complete";
                case PASSENGER_RIDE_ACCEPTED:
                    return "passenger_ride_accepted";
                case PASSENGER_RIDE_SENT:
                    return "passenger_ride_sent";
                default:
                    throw new AssertionError("Unknown state " + this);
            }
        }
    }

}