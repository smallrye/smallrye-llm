package dev.langchain4j.cdi.example.booking;

import java.time.LocalDate;
import java.util.Objects;

public class Booking {

    private String bookingNumber;
    private LocalDate start;
    private LocalDate end;
    private Customer customer;
    private boolean canceled = false;
    private String carModel;

    /**
     *
     */
    public Booking() {
        super();
    }

    /**
     * @param bookingNumber
     * @param start
     * @param end
     * @param customer
     * @param canceled
     * @param carModel
     */
    public Booking(String bookingNumber, LocalDate start, LocalDate end, Customer customer, boolean canceled,
            String carModel) {
        super();
        this.bookingNumber = bookingNumber;
        this.start = start;
        this.end = end;
        this.customer = customer;
        this.canceled = canceled;
        this.carModel = carModel;
    }

    /**
     * @return the bookingNumber
     */
    public String getBookingNumber() {
        return bookingNumber;
    }

    /**
     * @param bookingNumber the bookingNumber to set
     */
    public void setBookingNumber(String bookingNumber) {
        this.bookingNumber = bookingNumber;
    }

    /**
     * @return the start
     */
    public LocalDate getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(LocalDate start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public LocalDate getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(LocalDate end) {
        this.end = end;
    }

    /**
     * @return the customer
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * @param customer the customer to set
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    /**
     * @return the canceled
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * @param canceled the canceled to set
     */
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    /**
     * @return the carModel
     */
    public String getCarModel() {
        return carModel;
    }

    /**
     * @param carModel the carModel to set
     */
    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingNumber, canceled, carModel, customer, end, start);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Booking other = (Booking) obj;
        return Objects.equals(bookingNumber, other.bookingNumber) && canceled == other.canceled
                && Objects.equals(carModel, other.carModel) && Objects.equals(customer, other.customer)
                && Objects.equals(end, other.end) && Objects.equals(start, other.start);
    }

    @Override
    public String toString() {
        return "Booking [bookingNumber=" + bookingNumber + ", start=" + start + ", end=" + end + ", customer="
                + customer + ", canceled=" + canceled + ", carModel=" + carModel + "]";
    }
}
