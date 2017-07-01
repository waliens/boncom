package be.mormont.iacf.boncom.data;

import java.util.ArrayList;

/**
 * Created by Romain on 28-06-17.
 * An entity is for instance a provider.
 */
public class Entity {
    private String name;
    private Address address;
    private ArrayList<String> phoneNumbers;

    public Entity(String name, Address address, ArrayList<String> phoneNumbers) {
        this.name = name;
        this.address = address;
        this.phoneNumbers = phoneNumbers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public ArrayList<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(ArrayList<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }
}
