package org.memnar.backend.memnarjar.model;

import lombok.Getter;
import org.springframework.messaging.handler.annotation.SendTo;

@Getter
@SendTo
public class MemnarJarData {

    private String name;
    private String base64;

    public MemnarJarData() {}

    public MemnarJarData(String name, String base64) {
        this.name = name;
        this.base64 = base64;
    }
}
