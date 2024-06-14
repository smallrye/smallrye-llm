package io.jefrajames.booking;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    private String bookingNumber;
    private LocalDate start;
    private LocalDate end;
    private Customer customer;
    private boolean canceled = false;
    private String carModel;
    
}
