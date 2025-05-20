package com.infernokun.infernoctf.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum Role {
    MEMBER("Member"),
    CREATOR("Creator"),
    FACILITATOR("Facilitator"),
    ADMIN("Admin"),
    DEVELOPER("Developer");

    private final String value;

    Role(final String value) {
        this.value = value;
    }

    @JsonValue
    final String value() {
        return this.value;
    }
}
