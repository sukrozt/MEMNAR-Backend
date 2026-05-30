package org.memnar.backend.memnarjar.controller;

import org.memnar.memnar.pnarpp.algorithm.PNARpp;

public class MemnarRunner {
    public static void main(String[] args) {
        try {
            // Yeni ve tertemiz JVM açıldığında doğrudan algoritmayı başlatıyoruz
            PNARpp.runAlgorithm();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1); // Bir hata olursa hata koduyla çık
        }
    }
}