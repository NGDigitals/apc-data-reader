package com.ngdigitals.apc.data.reader.model;

import lombok.Data;

@Data
public class Voter {
    String VIN;
    String lastName;
    String otherNames;
    String occupation;
    String gender;
    Integer age;
    String state;
    String lga;
    String regArea;
    String pollingUnit;
    String picture;

    public Voter(){}
}
