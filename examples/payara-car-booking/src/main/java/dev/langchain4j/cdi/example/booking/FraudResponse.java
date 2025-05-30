package dev.langchain4j.cdi.example.booking;

import java.util.List;

// Warning: Java Record not supported by Google JSON (used by LangChain4J)
public class FraudResponse {
    private String customerName;
    private String customerSurname;
    private boolean fraudDetected;
    private List<String> bookingIds;
    
	/**
	 * @return the customerName
	 */
	public String getCustomerName() {
		return customerName;
	}
	
	/**
	 * @param customerName the customerName to set
	 */
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	
	/**
	 * @return the customerSurname
	 */
	public String getCustomerSurname() {
		return customerSurname;
	}
	
	/**
	 * @param customerSurname the customerSurname to set
	 */
	public void setCustomerSurname(String customerSurname) {
		this.customerSurname = customerSurname;
	}
	
	/**
	 * @return the fraudDetected
	 */
	public boolean isFraudDetected() {
		return fraudDetected;
	}
	
	/**
	 * @param fraudDetected the fraudDetected to set
	 */
	public void setFraudDetected(boolean fraudDetected) {
		this.fraudDetected = fraudDetected;
	}
	
	/**
	 * @return the bookingIds
	 */
	public List<String> getBookingIds() {
		return bookingIds;
	}
	
	/**
	 * @param bookingIds the bookingIds to set
	 */
	public void setBookingIds(List<String> bookingIds) {
		this.bookingIds = bookingIds;
	}
}
