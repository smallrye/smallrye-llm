package dev.langchain4j.cdi.example.booking;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.logging.Logger;

import dev.langchain4j.agent.tool.Tool;

@ApplicationScoped
public class BookingService {

    private static final Logger LOGGER = Logger.getLogger(BookingService.class.getName());

    // Pseudo database
    private static final Map<String, Booking> BOOKINGS = new HashMap<>();
    static {
        // James Bond: hero customer!
        BOOKINGS.put("123-456", new Booking("123-456", LocalDate.now().plusDays(1), LocalDate.now().plusDays(7),
                new Customer("James", "Bond"), false, "Aston Martin")); // Not cancelable: too late
        BOOKINGS.put("234-567", new Booking("234-567", LocalDate.now().plusDays(10), LocalDate.now().plusDays(12),
                new Customer("James", "Bond"), false, "Renault")); // Not cancelable: too short
        BOOKINGS.put("345-678", new Booking("345-678", LocalDate.now().plusDays(14), LocalDate.now().plusDays(20),
                new Customer("James", "Bond"), false, "Porsche")); // Cancelable
        // Emilio Largo: villain frauder!
        BOOKINGS.put("456-789", new Booking("456-789", LocalDate.now().plusDays(10), LocalDate.now().plusDays(20),
                new Customer("Largo", "Emilio"), false, "Porsche")); // Cancelable
        BOOKINGS.put("567-890", new Booking("567-890", LocalDate.now().plusDays(11), LocalDate.now().plusDays(16),
                new Customer("Largo", "Emilio"), false, "BMW")); // Cancelable
    }

    // Simulate database accesses
    private Booking checkBookingExists(String bookingNumber, String name, String surname) {
        Booking booking = BOOKINGS.get(bookingNumber);
        if (booking == null || !booking.getCustomer().getName().equals(name)
                || !booking.getCustomer().getSurname().equals(surname)) {
            throw new BookingNotFoundException(bookingNumber);
        }
        return booking;
    }

    @Tool("Get booking details given a booking number and customer name and surname")
    public Booking getBookingDetails(String bookingNumber, String name, String surname) {
        LOGGER.info("DEMO: Calling Tool-getBookingDetails: " + bookingNumber + " and customer: "
                + name + " " + surname);
        return checkBookingExists(bookingNumber, name, surname);
    }

    @Tool("Get all booking ids for a customer given his name and surname")
    public List<String> getBookingsForCustomer(String name, String surname) {
        LOGGER.info("DEMO: Calling Tool-getBookingsForCustomer: " + name + " " + surname);
        Customer customer = new Customer(name, surname);
        return BOOKINGS.values()
                .stream()
                .filter(booking -> booking.getCustomer().equals(customer))
                .map(Booking::getBookingNumber)
                .collect(Collectors.toList());
    }

    public void checkCancelPolicy(Booking booking) {

        // Reservations can be cancelled up to 7 days prior to the start of the booking
        // period
        if (LocalDate.now().plusDays(7).isAfter(booking.getStart())) {
            throw new BookingCannotBeCanceledException(booking.getBookingNumber() + " Too late");
        }

        // If the booking period is less than 3 days, cancellations are not permitted.
        if (booking.getEnd().compareTo(booking.getStart().plusDays(3)) < 0) {
            throw new BookingCannotBeCanceledException(booking.getBookingNumber() + " Too short");
        }

    }

    @Tool("Cancel a booking given its booking number and customer name and surname")
    public Booking cancelBooking(String bookingNumber, String name, String surname) {
        LOGGER.info("DEMO: Calling Tool-cancelBooking " + bookingNumber + " for customer: " + name
                + " " + surname);

        Booking booking = checkBookingExists(bookingNumber, name, surname);

        if (booking.isCanceled())
            throw new BookingCannotBeCanceledException(bookingNumber);

        checkCancelPolicy(booking);

        booking.setCanceled(true);

        return booking;
    }

}
