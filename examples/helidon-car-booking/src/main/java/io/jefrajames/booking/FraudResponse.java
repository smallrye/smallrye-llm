package io.jefrajames.booking;

import java.util.List;

import lombok.Data;

// Warning: Java Record not supported by Google JSON (used by LangChain4J)
@Data
public class FraudResponse {
    private String customerName; 
    private String customerSurname; 
    private boolean fraudDetected;
    private  List<String> bookingIds;
}
