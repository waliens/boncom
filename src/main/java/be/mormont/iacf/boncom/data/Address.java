package be.mormont.iacf.boncom.data;

/**
 * Created by Romain on 28-06-17.
 * This is a class.
 */
public class Address {
    private String street, number, box, postCode, city;

    public Address(String street, String number, String box, String postCode, String city) {
        this.street = street;
        this.number = number;
        this.box = box;
        this.postCode = postCode;
        this.city = city;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(street);
        if (box != null && !box.isEmpty()) {
            builder.append(", ").append(box);
        }
        builder.append(", ").append(postCode)
                .append(", ").append(city);
        return builder.toString();
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getBox() {
        return box;
    }

    public void setBox(String box) {
        this.box = box;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
