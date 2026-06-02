package org.memnar.backend.memnarjar.service;

import org.memnar.backend.memnarjar.model.ConfigData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class ResultsService {

    private final ConfigService configService;

    @Autowired
    public ResultsService(ConfigService configService) {
        this.configService = configService;
    }

    public File getLatestResultsFile() {
        return getLatestResultsFile("MutualExclusiveSets.html");
    }

    public File getLatestResultsFile(String targetFileName) {
        ConfigData config = configService.getConfig();

        // 1. Construct the expected dynamic output folder based on the algorithm's naming convention
        String rawPath = config.getDatasetName() != null ? config.getDatasetName() : "mutation_data"; 
        String filteredPath = rawPath;
        if (config.isUnformatted() && !rawPath.endsWith("_filtered.txt")) {
            filteredPath = rawPath.endsWith(".txt") ? rawPath.substring(0, rawPath.length() - 4) + "_filtered.txt" : rawPath + "_filtered.txt";
        }
        
        String datasetShortName = filteredPath;
        if (datasetShortName.contains("/")) datasetShortName = datasetShortName.substring(datasetShortName.lastIndexOf("/") + 1);
        if (datasetShortName.endsWith(".txt")) datasetShortName = datasetShortName.substring(0, datasetShortName.length() - 4);

        String suppStr = String.valueOf(config.getMinSupp()).replace(".", "");
        String confStr = String.valueOf(config.getMinConf()).replace(".", "");
        String expectedDirName = datasetShortName + "supp" + suppStr + "minconf" + confStr;

        File outputDir = new File("output");
        File latestOutputFile = null;
        long latestTime = 0;

        if (outputDir.exists() && outputDir.isDirectory()) {
            // A. Attempt to fetch the exact file generated for the current configuration
            File exactDir = new File(outputDir, expectedDirName);
            if (exactDir.exists() && exactDir.isDirectory()) {
                File exactHtml = new File(exactDir, targetFileName);
                if (exactHtml.exists()) {
                    // System.out.println("[DEBUG - ResultsService] Found exact HTML results at: " + exactHtml.getPath());
                    return exactHtml;
                }
            }
            // System.out.println("[DEBUG - ResultsService] Exact path not found: " + exactDir.getPath() + ". Falling back to latest.");

            // B. Fallback: Check for files directly in the 'output' directory
            File[] directFiles = outputDir.listFiles((dir, name) -> name.equals(targetFileName));
            if (directFiles != null) {
                for (File file : directFiles) {
                    if (file.lastModified() > latestTime) {
                        latestOutputFile = file;
                        latestTime = file.lastModified();
                    }
                }
            }

            // C. Fallback: Check for files in subdirectories
            File[] subDirs = outputDir.listFiles(File::isDirectory);
            if (subDirs != null) {
                for (File subDir : subDirs) {
                    File htmlFile = new File(subDir, targetFileName);
                    if (htmlFile.exists() && htmlFile.lastModified() > latestTime) {
                        latestOutputFile = htmlFile;
                        latestTime = htmlFile.lastModified();
                    }
                }
            }
        }

        return latestOutputFile;
    }

    public String getLatestResultsContent(String targetFileName) throws IOException {
        File file = getLatestResultsFile(targetFileName);
        if (file != null) {
            String htmlContent = new String(Files.readAllBytes(file.toPath()));

            String baseTag = "<base href=\"http://localhost:8080/\">\n";
            if (htmlContent.toLowerCase().contains("<!doctype html>")) {
                htmlContent = htmlContent.replaceFirst("(?i)<!doctype html>", "<!DOCTYPE html>\n" + baseTag);
            } else if (htmlContent.toLowerCase().contains("<html")) {
                htmlContent = htmlContent.replaceFirst("(?i)<html[^>]*>", "$0\n" + baseTag);
            } else {
                // Hiçbir ana etiket yoksa (ham D3 çıktısı ise) dosyanın en başına ekle
                htmlContent = baseTag + htmlContent;
            }
            return htmlContent;
        }
        return null;
    }

    public String getLatestResultsContent() throws IOException {
        return getLatestResultsContent("MutualExclusiveSets.html");
    }
}