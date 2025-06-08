package com.example.register.register.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VolumeType {
    BASIC(           "Podstawowy czas pracy"),
    OVERTIME_PAID(   "Nadgodziny płatne"),
    OVERTIME_OFF(    "Nadgodziny do odbioru"),
    DEDUCT_PARTIAL(  "Odebrane częściowo"),
    DEDUCT_FULL_DAY( "Odebrano cały dzień");

    private final String label;

    VolumeType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static VolumeType forValue(String value) {
        for (VolumeType vt : values()) {
            // akceptujemy nazwę enuma lub etykietę
            if (vt.name().equalsIgnoreCase(value) ||
                    vt.label.equalsIgnoreCase(value)) {
                return vt;
            }
        }
        throw new IllegalArgumentException("Nieprawidłowa wartość VolumeType: " + value);
    }
}
