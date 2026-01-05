package org.memnar.backend.memnarjar.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MemnarJarStatus {

    private String message;
    private String output;

    public MemnarJarStatus() {}

    public MemnarJarStatus(String message, String output) {
        this.message = message;
        this.output = output;
    }
}
