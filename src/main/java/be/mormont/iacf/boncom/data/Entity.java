package be.mormont.iacf.boncom.data;

/**
 * Created by Romain on 28-06-17.
 * An entity is for instance a provider.
 */
public class Entity implements Comparable<Entity> {
    private long id;
    private String name;
    private Address address;
    private String[] phoneNumbers;
    private String customerNb;

    public Entity(String name, Address address, String[] phoneNumbers, String customerNb) {
        this(-1, name, address, phoneNumbers, customerNb);
    }

    public Entity(long id, String name, Address address, String[] phoneNumbers, String customerNb) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phoneNumbers = phoneNumbers;
        this.customerNb = customerNb;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String[] getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(String[] phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public String getPhonesAsString() {
        return phoneNumbers == null ? null : String.join(",", phoneNumbers);
    }

    public String getCustomerNb() {
        return customerNb;
    }

    public void setCustomerNb(String customerNb) {
        this.customerNb = customerNb;
    }

    @Override
    public int compareTo(Entity o) {
        return (int) (getId() - o.getId());
    }

    @Override
    public int hashCode() {
        return (int)id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Entity && ((Entity) obj).compareTo(this) == 0;
    }
}
