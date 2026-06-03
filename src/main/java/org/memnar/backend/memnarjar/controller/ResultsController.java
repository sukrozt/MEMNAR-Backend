package org.memnar.backend.memnarjar.controller;

import org.memnar.backend.memnarjar.service.ResultsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@CrossOrigin
public class ResultsController {

    private final ResultsService resultsService;

    @Autowired
    public ResultsController(ResultsService resultsService) {
        this.resultsService = resultsService;
    }

    @GetMapping(value = "/api/results/normal", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> getNormalResults() {
        File file = resultsService.getLatestResultsFile();
        if (file != null && file.exists()) {
            return ResponseEntity.ok(new FileSystemResource(file));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/api/results/conditional", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> getConditionalResults() {
        File file = resultsService.getLatestResultsFile("ConditionalMutualExclusiveSets.html");
        if (file != null && file.exists()) {
            return ResponseEntity.ok(new FileSystemResource(file));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/api/results/rules", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> getRules() {
        File file = resultsService.getLatestResultsFile("rules.txt");
        if (file != null && file.exists()) {
            return ResponseEntity.ok(new FileSystemResource(file));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/api/results/itemsets", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> getItemSets() {
        File file = resultsService.getLatestResultsFile("itemsets.txt");
        if (file != null && file.exists()) {
            return ResponseEntity.ok(new FileSystemResource(file));
        }
        return ResponseEntity.notFound().build();
    }

    // Grafiklerin her iki adresten de (path kayması sorunu olmadan) çekilebilmesi için
    @GetMapping(value = {"/api/IndividualFigures/{filename:.+}", "/api/results/IndividualFigures/{filename:.+}", "/api/results/normal/IndividualFigures/{filename:.+}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource> getNormalFigure(@PathVariable String filename) {
        File htmlFile = resultsService.getLatestResultsFile();
        if (htmlFile != null && htmlFile.exists()) {
            File figureFile = new File(htmlFile.getParentFile(), "IndividualFigures" + File.separator + filename);
            if (figureFile.exists()) {
                return ResponseEntity.ok(new FileSystemResource(figureFile));
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = {"/api/ConditionalIndividualFigures/{filename:.+}", "/api/results/ConditionalIndividualFigures/{filename:.+}", "/api/results/conditional/ConditionalIndividualFigures/{filename:.+}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource> getConditionalFigure(@PathVariable String filename) {
        File htmlFile = resultsService.getLatestResultsFile();
        if (htmlFile != null && htmlFile.exists()) {
            File figureFile = new File(htmlFile.getParentFile(), "ConditionalIndividualFigures" + File.separator + filename);
            if (figureFile.exists()) {
                return ResponseEntity.ok(new FileSystemResource(figureFile));
            }
        }
        return ResponseEntity.notFound().build();
    }
}
