package org.memnar.backend.memnarjar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates Getters, Setters, toString, etc.
@Builder
@NoArgsConstructor // Needed for JSON deserialization (Spring)
@AllArgsConstructor // Needed for Builder
public class FileDTO {
    private String name;
    private String base64;
    private int chunkIndex;
    private int totalChunks;
}