package io.jefrajames.booking;

public class BookingAlreadyCanceledException extends RuntimeException {

    public BookingAlreadyCanceledException(String bookingNumber) {
        super("Booking " + bookingNumber + " already canceled");
    }
    
}
