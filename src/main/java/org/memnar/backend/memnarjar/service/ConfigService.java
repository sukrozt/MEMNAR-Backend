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
    }

    public void writeConfigFile() throws IOException {
        File targetResDir = new File("res");
        if (!targetResDir.exists()) targetResDir.mkdirs();

        setupResources(targetResDir);
        File configFile = new File(targetResDir, "config.properties");
        writePropertiesToFile(configFile);
        File defaultFile = new File(targetResDir, "default.properties");
        writePropertiesToFile(defaultFile);
    }
private void setupResources(File targetResDir) {
        // A. Copy HTML Template
        copyRecursive(new File("src/main/resources/res/HTMLOutputTemplates"), new File(targetResDir, "HTMLOutputTemplates"));

        // B. Copy Libraries (Recursively copies d3.min.js and folders)
        File targetLibDir = new File("libraries");
        copyRecursive(new File("src/main/resources/libraries"), targetLibDir);
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

    private void copyFile(String resourcePath, File destFile) {
        try {
            if (!destFile.getParentFile().exists()) destFile.getParentFile().mkdirs();
            
            var source = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (source != null) {
                Files.copy(source, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Copied resource: " + resourcePath);
                source.close();
            } else {
                System.err.println("WARNING: Resource not found in src: " + resourcePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to copy " + resourcePath + ": " + e.getMessage());
        }
    }

    private void setupHtmlTemplate(File targetResDir) {
        File targetTemplateDir = new File(targetResDir, "HTMLOutputTemplates");
        if (!targetTemplateDir.exists()) targetTemplateDir.mkdirs();
        File targetFile = new File(targetTemplateDir, "HTMLOutputTemplate.html");
        File sourceFile = new File("src/main/resources/res/HTMLOutputTemplates/HTMLOutputTemplate.html");

        try {
            if (sourceFile.exists()) {
                Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Template copied: " + sourceFile.getPath() + " -> " + targetFile.getPath());
            } else {
                System.err.println("CRITICAL WARNING: Source template not found at: " + sourceFile.getAbsolutePath());
                System.err.println("Please check if the file exists in your 'src/main/resources/res/HTMLOutputTemplates/' folder.");
            }
        } catch (IOException e) {
            System.err.println("Failed to copy HTML template: " + e.getMessage());
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
            String basePath = "mutation_data/braingene.txt"; 
            writer.println("DatasetName=braingene");
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