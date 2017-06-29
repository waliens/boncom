package be.mormont.iacf.boncom.data;

/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class Address {
    public String street, number, box, postCode, city;

    public Address(String street, String number, String box, String postCode, String city) {
        this.street = street;
        this.number = number;
        this.box = box;
        this.postCode = postCode;
        this.city = city;
    }
}
