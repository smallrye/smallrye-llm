package io.jefrajames.booking;

public class BookingCannotBeCanceledException extends RuntimeException {

    public BookingCannotBeCanceledException(String bookingNumber) {
        super("Booking " + bookingNumber + " cannot be canceled");
    }
    
}
