package org.memnar.backend.memnarjar.controller;

import jakarta.annotation.Nonnull;
import org.memnar.backend.memnarjar.model.MemnarJarStatus;
import org.memnar.backend.memnarjar.service.DataConverterService;
import org.memnar.backend.memnarjar.service.ResultsService;
import org.memnar.memnar.pnarpp.algorithm.PNARpp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

@RestController
@CrossOrigin
public class MemnarJarController {

    private final DataConverterService dataConverterService;
    private final ResultsService resultsService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public MemnarJarController(DataConverterService dataConverterService, ResultsService resultsService, SimpMessagingTemplate messagingTemplate) {
        this.dataConverterService = dataConverterService;
        this.resultsService = resultsService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/memnarjar/start")
    @SendTo("/memnarjar/status")
    public MemnarJarStatus runJar() throws Exception {

        // Logging
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        PrintStream newOut = getPrintStream(oldOut, "INFO");
        PrintStream newErr = getPrintStream(oldErr, "ERROR");

        System.setOut(newOut);
        System.setErr(newErr);
        
        System.out.println("\n ----- PREPARING TO RUN ----- \n");

        // Alert the client immediately that the algorithm has started running
        messagingTemplate.convertAndSend("/memnarjar/status", new MemnarJarStatus("Running", "The MEMNAR algorithm is currently executing..."));

        try {
            // Format the data first if needed (this also securely handles writing the necessary config files)
            dataConverterService.processFormatting();
        } catch (IllegalStateException e) {
            System.out.flush();
            System.err.flush();
            System.setOut(oldOut);
            System.setErr(oldErr);
            return new MemnarJarStatus("Error", e.getMessage());
        }

        long startTime = System.currentTimeMillis();
        
        // Artık Spring Boot ile aynı JVM'de değil, ProcessBuilder ile yalıtılmış terminalde çalıştırıyoruz.
        try {
            String javaHome = System.getProperty("java.home");
            String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
            String classpath = System.getProperty("java.class.path");

            // Kendi yazdığımız özel Main metoduna sahip çalıştırıcı sınıfımızı (MemnarRunner) tetikliyoruz.
            // Bu sınıfın içindeki main metodu arka planda temiz bir şekilde PNARpp.runAlgorithm() metodunu çağıracak.
            ProcessBuilder pb = new ProcessBuilder(javaBin, "-cp", classpath, "org.memnar.backend.memnarjar.controller.MemnarRunner");
            pb.redirectErrorStream(true); // Hata (err) ve Standart (out) logları birleştirir.

            Process process = pb.start();

            // Alt sürecin konsol çıktılarını anlık olarak okuyup web sayfasına basıyoruz
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line); // System.out'u yakaladığımız için bu loglar anında frontend'e gider!
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Algoritma beklenmedik bir şekilde kapandı. Çıkış Kodu: " + exitCode);
            }
        } catch (Exception e) {
            System.err.println("ProcessBuilder çalıştırılırken hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("\n -----FINISHED----- \n ELAPSED TIME : " + totalTime + " ms");


        System.out.flush();
        System.err.flush();
        System.setOut(oldOut);
        System.setErr(oldErr);

        try {
            String output = resultsService.getLatestResultsContent();
            if (output != null) {
                return new MemnarJarStatus("FINISHED in " + totalTime + " ms", output);
            } else {
                return new MemnarJarStatus("Error", "Algorithm finished, but output file not found in 'output' directory.");
            }
        } catch (Exception e) {
            return new MemnarJarStatus("Error", "Algorithm finished, but failed to read the output file.");
        }

    }

    @Nonnull
    private PrintStream getPrintStream(PrintStream old, String level) {
        OutputStream os = new OutputStream() {
            private final StringBuilder buffer = new StringBuilder();

            @Override
            public void write(int b) {
                char c = (char) b;

                if (c == '\n') {
                    String line = buffer.toString();
                    sendLog("[" + level + "] " + line);
                    old.println(line);
                    buffer.setLength(0);
                } else if (c != '\r') {
                    buffer.append(c);
                }
            }
        };
        return new PrintStream(os);
    }

    private void sendLog(String log) {
        messagingTemplate.convertAndSend("/memnarjar/status", new MemnarJarStatus("MEMNAR", log));
    }
}