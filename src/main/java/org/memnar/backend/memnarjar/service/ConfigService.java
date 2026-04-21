package org.memnar.backend.memnarjar.service;

import org.memnar.backend.memnarjar.model.ConfigData;
import org.springframework.stereotype.Service;
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

        File configFile = new File(datasetMgrDir, "config.properties");
        writePropertiesToFile(configFile);
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

    private void writePropertiesToFile(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("# Generated Config by Web Interface");

            // --- USER SETTINGS ---
            writer.println("minsupp=" + currentConfig.getMinSupp());
            writer.println("minconf=" + currentConfig.getMinConf());
            writer.println("FindMutualExclusiveSets=" + currentConfig.isFindMutualExclusiveSets());
            writer.println("FindConditionalMutualExclusiveSets=" + currentConfig.isFindConditionalMutualExclusiveSets());
            writer.println("minzscore=" + currentConfig.getMinZScore());
            writer.println("MaxSetSize=" + currentConfig.getMaxSetSize());
            writer.println("PvalueCutoff=" + currentConfig.getPValueCutoff());
            writer.println("sortByPathway=" + currentConfig.isSortByPathway());
            writer.println("tumorsOfInterest=" + currentConfig.getTumorsOfInterest());
            writer.println("TimeLimit=" + currentConfig.getTimeLimit());

            // --- FILE PATHS ---
            String basePath = currentConfig.getDatasetName() != null ? currentConfig.getDatasetName() : "mutation_data"; 
            
            // Derive short name for DatasetName
            String datasetShortName = basePath;
            if (datasetShortName.contains("/")) datasetShortName = datasetShortName.substring(datasetShortName.lastIndexOf("/") + 1);
            if (datasetShortName.endsWith(".txt")) datasetShortName = datasetShortName.substring(0, datasetShortName.length() - 4);
            if (datasetShortName.endsWith(".zip")) datasetShortName = datasetShortName.substring(0, datasetShortName.length() - 4);

            writer.println("DatasetName=" + datasetShortName);
            writer.println("FPGInputPathP1=" + basePath);
            writer.println("Rawinput=" + basePath);

            // --- DEFAULTS ---
            writer.println("PNARppItemsetsPath=itemsets.txt");
            writer.println("PNARppRulesPath=rules.txt");
            writer.println("MutualExclusiveSetsOutputPath=MutualExclusiveSets.txt");
            writer.println("SecondTypeMutualExclusiveSetsOutputPath=MutualExclusiveSetsType2.txt");
            writer.println("printItemsets=true");
            writer.println("printRules=true");
            writer.println("insertPathwaySimilaritiesToItemsets=true");
            writer.println("insertPathwaySimilaritiesToRules=true");
            writer.println("createHTMLoutput=true");
            writer.println("ConditionalMutualExclusiveSetsHTMLOutputPath=ConditionalMutualExclusiveSets.html");
            writer.println("MutualExclusiveSetsHTMLOutputPath=MutualExclusiveSets.html");
            writer.println("mutMtxDataset=other");
        }
    }
}