package org.memnar.backend.memnarjar.service;

import org.memnar.backend.memnarjar.model.ConfigData;
import org.springframework.stereotype.Service;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Service
public class ConfigService {

    private ConfigData currentConfig = new ConfigData();

    public ConfigData getConfig() {
        return currentConfig;
    }

    public void updateConfig(ConfigData newConfig) {
        System.out.println("[DEBUG - ConfigService] updateConfig called! Old DatasetName: " + this.currentConfig.getDatasetName() + " | New DatasetName: " + newConfig.getDatasetName());
        
        // BUGFIX: Prevent the frontend from overwriting a valid dataset name with null or empty values
        String incomingName = newConfig.getDatasetName();
        if (incomingName == null || incomingName.trim().isEmpty() || incomingName.equals("mutation_data")) {
            newConfig.setDatasetName(this.currentConfig.getDatasetName());
        }

        this.currentConfig = newConfig;
        try {
            persistConfigFile();
            System.out.println("Configuration updated and saved to file.");
        } catch (IOException e) {
            System.err.println("ERROR: Failed to save config.properties after update from web interface: " + e.getMessage());
        }
    }

    public void writeConfigFile() throws IOException {
        File targetResDir = new File("res");
        if (!targetResDir.exists()) targetResDir.mkdirs();

        setupResources(targetResDir);
        persistConfigFile();
    }

    private void persistConfigFile() throws IOException {
        File datasetMgrDir = new File("res/datasetmgr");
        if (!datasetMgrDir.exists()) datasetMgrDir.mkdirs();

        File datasetMgrConfig = new File(datasetMgrDir, "config.properties");
        File mainConfigRes = new File("res", "config.properties");
        File mainConfigRoot = new File("config.properties"); // Fallback for the DataConverter JAR
        
        writeDatasetMgrConfig(datasetMgrConfig);
        writeMainConfig(mainConfigRes);
        writeMainConfig(mainConfigRoot);
    }
    private void setupResources(File targetResDir) {
        // A. Copy HTML Template
        copyRecursive(new File("res/HTMLOutputTemplates"), new File(targetResDir, "HTMLOutputTemplates"));

        // B. Copy Libraries (Recursively copies d3.min.js and folders)
        File targetLibDir = new File("libraries");
        copyRecursive(new File("libraries/gd3_mutmtx"), targetLibDir);
    }

    // --- RECURSIVE COPIER ---
    private void copyRecursive(File source, File dest) {
        if (source.isDirectory()) {
            if (!dest.exists()) dest.mkdirs();
            String[] files = source.list();
            if (files != null) {
                for (String file : files) {
                    copyRecursive(new File(source, file), new File(dest, file));
                }
            }
        } else {
            try {
                // Ensure parent dir exists
                if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
                Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("   -> Copied: " + dest.getName());
            } catch (IOException e) {
                System.err.println("   ❌ Failed to copy " + source.getName() + ": " + e.getMessage());
            }
        }
    }

    private void writeMainConfig(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            
            String rawPath = currentConfig.getDatasetName() != null ? currentConfig.getDatasetName() : "mutation_data"; 
            String filteredPath = rawPath;
            
            // Pre-calculate the filtered path so the main algorithm knows where to look
            if (currentConfig.isUnformatted() && !rawPath.endsWith("_filtered.txt")) {
                filteredPath = rawPath.endsWith(".txt") ? rawPath.substring(0, rawPath.length() - 4) + "_filtered.txt" : rawPath + "_filtered.txt";
            }
            
            String datasetShortName = filteredPath;
            if (datasetShortName.contains("/")) datasetShortName = datasetShortName.substring(datasetShortName.lastIndexOf("/") + 1);
            if (datasetShortName.endsWith(".txt")) datasetShortName = datasetShortName.substring(0, datasetShortName.length() - 4);

            writer.println("minsupp=" + currentConfig.getMinSupp());
            writer.println("minconf=" + currentConfig.getMinConf());
            writer.println("minzscore=" + currentConfig.getMinZScore());
            writer.println("sortByPathway=" + currentConfig.isSortByPathway());
            
            writer.println("PNARppItemsetsPath=itemsets.txt");
            writer.println("PNARppRulesPath=rules.txt");
            writer.println("MutualExclusiveSetsOutputPath=MutualExclusiveSets.txt");
            writer.println("SecondTypeMutualExclusiveSetsOutputPath=MutualExclusiveSetsType2.txt");
            writer.println("printItemsets=true");
            writer.println("printRules=true");
            writer.println("insertPathwaySimilaritiesToItemsets=true");
            writer.println("insertPathwaySimilaritiesToRules=true");
            writer.println("FindMutualExclusiveSets=" + currentConfig.isFindMutualExclusiveSets());
            writer.println("FindConditionalMutualExclusiveSets=" + currentConfig.isFindConditionalMutualExclusiveSets());
            writer.println("FindCustomRules=false");
            writer.println("MaxSetSize=" + currentConfig.getMaxSetSize());
            writer.println("MaxPositiveSize=4");
            writer.println("MaxNegativeSize=4");
            
            writer.println("DatasetName=" + datasetShortName);
            writer.println("FPGInputPathP1=" + filteredPath);
            
            writer.println("shouldsubtract=false");
            writer.println("totalnumofruns=100");
            writer.println("coverages=0.11,0.13,0.15,0.17,0.19,0.21,0.23,0.25");
            writer.println("balanced=true");
            writer.println("TuneParameters=true");
            writer.println("suppstotry=0.02");
            writer.println("confstotry=0.4");
            writer.println("TimeLimit=" + currentConfig.getTimeLimit());
            writer.println("numoftops=10");
            writer.println("createHTMLoutput=true");
            writer.println("ConditionalMutualExclusiveSetsHTMLOutputPath=ConditionalMutualExclusiveSets.html");
            writer.println("MutualExclusiveSetsHTMLOutputPath=MutualExclusiveSets.html");
            writer.println("mutMtxDataset=other");
            writer.println("tumorsOfInterest=" + currentConfig.getTumorsOfInterest());
            writer.println("PvalueCutoff=" + currentConfig.getPValueCutoff());
        }
    }

    private void writeDatasetMgrConfig(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            
            String rawPath = currentConfig.getDatasetName() != null ? currentConfig.getDatasetName() : "mutation_data"; 
            String filteredPath = rawPath;
            if (currentConfig.isUnformatted() && !rawPath.endsWith("_filtered.txt")) {
                filteredPath = rawPath.endsWith(".txt") ? rawPath.substring(0, rawPath.length() - 4) + "_filtered.txt" : rawPath + "_filtered.txt";
            }

            writer.println("inputPath=" + rawPath);
            writer.println("outputPath=" + filteredPath);
            writer.println("sortUsingSupportCount=true");
            writer.println("separateMutationType=false");
            writer.println("removeChainMutations=true");
            writer.println("removeMutationsWithMutationType=false");
            writer.println("removeExistingDataAndAddAbsentData=false");
            writer.println("splitMutationsWithComma=false");
            writer.println("addSpace=false");
            
            /*// Hardcoded PanCancer parameters as expected by the jar internally
            writer.println("inputPathPanCan=mutation_data/PanCancer/Raw/PanCan.txt");
            writer.println("outputPathPanCan=mutation_data/PanCancer/PanCan-filtered-data.txt");
            writer.println("sortEachPatientGenesUsingTheirSupportValue=true");
            writer.println("printDataSeparatedAccordingToTheDatasetType=false");*/
        }
    }
}