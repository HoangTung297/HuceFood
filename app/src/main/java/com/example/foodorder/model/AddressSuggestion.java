package com.example.foodorder.model;

public class AddressSuggestion {
    private String fullAddress;
    private String streetNumber;
    private String street;
    private String ward;
    private String district;
    private String city;

    public AddressSuggestion() {}

    public AddressSuggestion(String fullAddress, String streetNumber, String street, String ward, String district, String city) {
        this.fullAddress = fullAddress;
        this.streetNumber = streetNumber;
        this.street = street;
        this.ward = ward;
        this.district = district;
        this.city = city;
    }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }
    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getShortAddress() {
        if (street != null && district != null) {
            return street + ", " + district;
        }
        return fullAddress;
    }
}