package io.jefrajames.booking;

import java.util.Objects;

//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@EqualsAndHashCode(of = { "name", "surname" })
public class Customer {
    private String name;
    private String surname;

    /**
     *
     */
    public Customer() {
        super();
        //TODO Auto-generated constructor stub
    }

    /**
     * @param name
     * @param surname
     */
    public Customer(String name, String surname) {
        super();
        this.name = name;
        this.surname = surname;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, surname);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Customer other = (Customer) obj;
        return Objects.equals(name, other.name) && Objects.equals(surname, other.surname);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */

    @Override
    public String toString() {
        return "Customer [name=" + name + ", surname=" + surname + "]";
    }
}
