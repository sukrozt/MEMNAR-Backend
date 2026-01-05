package org.memnar.backend.memnarjar.model;

import lombok.Data; 
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class ConfigData {
    private String datasetName;
    private double minSupp;
    private double minConf;
    private boolean findMutualExclusiveSets;
    private boolean findConditionalMutualExclusiveSets;
    private double minZScore;
    private int maxSetSize;
    private double pValueCutoff;
    private boolean sortByPathway;
    private String tumorsOfInterest;
    private int timeLimit;
}