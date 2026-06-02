package org.memnar.backend.security.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_configs")
public class UserConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    @Column(nullable = false)
    private String datasetName;

    @Column(nullable = false)
    private double minSupp;

    @Column(nullable = false)
    private double minConf;
    
    @Column(nullable = false)
    private double minZScore;
    
    @Column(nullable = false)
    private int maxSetSize;

    @Column(nullable = false)
    private double pValueCutoff;

    @Column(nullable = false)
    private boolean isUnformatted;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String datasetName) { 
        if (datasetName != null) {
            String shortName = datasetName;
            // Dosya yolunu temizle (örn: /home/.../BRCA-filtered-data.txt -> BRCA-filtered-data.txt)
            if (shortName.contains("/")) {
                shortName = shortName.substring(shortName.lastIndexOf("/") + 1);
            }
            if (shortName.contains("\\")) {
                shortName = shortName.substring(shortName.lastIndexOf("\\") + 1);
            }
            // .txt uzantısını temizle (BRCA-filtered-data.txt -> BRCA-filtered-data)
            if (shortName.toLowerCase().endsWith(".txt")) {
                shortName = shortName.substring(0, shortName.length() - 4);
            }
            this.datasetName = shortName;
        } else {
            this.datasetName = null;
        }
    }

    public double getMinSupp() { return minSupp; }
    public void setMinSupp(double minSupp) { this.minSupp = minSupp; }

    public double getMinConf() { return minConf; }
    public void setMinConf(double minConf) { this.minConf = minConf; }

    public double getMinZScore() { return minZScore; }
    public void setMinZScore(double minZScore) { this.minZScore = minZScore; }

    public int getMaxSetSize() { return maxSetSize; }
    public void setMaxSetSize(int maxSetSize) { this.maxSetSize = maxSetSize; }

    public double getPValueCutoff() { return pValueCutoff; }
    public void setPValueCutoff(double pValueCutoff) { this.pValueCutoff = pValueCutoff; }

    public boolean isUnformatted() { return isUnformatted; }
    public void setUnformatted(boolean isUnformatted) { this.isUnformatted = isUnformatted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}