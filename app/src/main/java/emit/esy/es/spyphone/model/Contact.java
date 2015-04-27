package emit.esy.es.spyphone.model;

import java.io.Serializable;

/**
 * Created by Emil Makovac on 27/04/2015.
 */
public class Contact implements Serializable {

    String fullName, phoneNumber;


    public Contact(String fullName, String phoneNumber) {

        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Contact contact = (Contact) o;

        if (!fullName.equals(contact.fullName)) return false;
        if (!phoneNumber.equals(contact.phoneNumber)) return false;

        return true;
    }

}
