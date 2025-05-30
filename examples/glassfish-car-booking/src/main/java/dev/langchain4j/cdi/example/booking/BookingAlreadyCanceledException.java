package dev.langchain4j.cdi.example.booking;

public class BookingAlreadyCanceledException extends RuntimeException {

    public BookingAlreadyCanceledException(String bookingNumber) {
        super("Booking " + bookingNumber + " already canceled");
    }

}
