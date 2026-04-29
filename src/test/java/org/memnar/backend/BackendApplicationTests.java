package org.memnar.backend;

import org.junit.jupiter.api.Test;
import org.memnar.backend.memnarjar.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BackendApplicationTests {

    @Autowired
    private FileService fileService;

    @Test
    void contextLoads() {
    }

    @Test
    void testRunDataConverter() throws Exception {
        // 1. Setup temporary input and output paths
        Path tempInputDir = Files.createTempDirectory("mock_tcga_input");
        Path tempOutputDir = Files.createTempDirectory("mock_tcga_output");
        
        // Note: TCGADataset and RunConverter likely expect specific dataset formats.
        // If the converter fails with empty files, replace this with a tiny mock of real TCGA data.
        File dummyInput = new File(tempInputDir.toFile(), "dummy_mutation_data.txt");
        Files.writeString(dummyInput.toPath(), "Hugo_Symbol\tChromosome\tStart_Position\tEnd_Position\tVariant_Classification\tTumor_Sample_Barcode\n");

        // 2. Execute the DataConverter via FileService
        // assertDoesNotThrow ensures the reflection invocation and the converter logic itself don't crash
        assertDoesNotThrow(() -> {
            fileService.runDataConverter(tempInputDir.toString(), tempOutputDir.toString());
        }, "RunConverter threw an exception during execution!");

        // 3. Verify that an output was generated in the output directory
        File[] generatedFiles = tempOutputDir.toFile().listFiles();
        assertTrue(generatedFiles != null && generatedFiles.length > 0, 
                "The converter completed, but no output files were generated in the output directory.");

        // Clean up mock data (Optional, but good practice)
        dummyInput.delete();
        tempInputDir.toFile().delete();
        // tempOutputDir cleaning would require recursively deleting contents first
    }
}
