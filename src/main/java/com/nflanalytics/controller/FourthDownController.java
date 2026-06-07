package com.nflanalytics.controller;

import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@RestController
@RequestMapping("/api/fourth-down")
@CrossOrigin(origins = "*")
public class FourthDownController {

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/analyze")
    public Map<String, Object> analyze(
            @RequestParam double scoreDiff,
            @RequestParam double secondsRemaining,
            @RequestParam double yardline,
            @RequestParam double ydstogo,
            @RequestParam(defaultValue = "2") double posTeamTimeouts,
            @RequestParam(defaultValue = "2") double defTeamTimeouts) {

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "/Users/nikhilsankaran/PycharmProjects/nfl-analytics/.venv/bin/python",
                    "/Users/nikhilsankaran/PycharmProjects/nfl-analytics/predict_fourth_down.py",
                    String.valueOf(scoreDiff),
                    String.valueOf(secondsRemaining),
                    String.valueOf(yardline),
                    String.valueOf(ydstogo),
                    String.valueOf(posTeamTimeouts),
                    String.valueOf(defTeamTimeouts)
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes()).trim();
            process.waitFor();

            return mapper.readValue(output, Map.class);

        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}