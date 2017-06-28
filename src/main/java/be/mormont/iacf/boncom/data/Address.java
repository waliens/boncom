package be.mormont.iacf.boncom.data;

/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class Address {
    public String road, number, box, postCode, town;

    public Address(String road, String number, String box, String postCode, String town) {
        this.road = road;
        this.number = number;
        this.box = box;
        this.postCode = postCode;
        this.town = town;
    }
}
