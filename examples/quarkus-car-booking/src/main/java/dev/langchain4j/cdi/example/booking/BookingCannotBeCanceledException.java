package dev.langchain4j.cdi.example.booking;

public class BookingCannotBeCanceledException extends RuntimeException {

    public BookingCannotBeCanceledException(String bookingNumber) {
        super("Booking " + bookingNumber + " cannot be canceled");
    }

}
