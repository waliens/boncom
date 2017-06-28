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
}
