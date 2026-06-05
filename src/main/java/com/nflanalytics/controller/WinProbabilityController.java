package com.nflanalytics.controller;

import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@RestController
@RequestMapping("/api/win-probability")
@CrossOrigin(origins = "*")
public class WinProbabilityController {

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/calculate")
    public Map<String, Object> calculateWinProbability(
            @RequestParam double scoreDiff,
            @RequestParam double secondsRemaining,
            @RequestParam(defaultValue = "2") double posTeamTimeouts,
            @RequestParam(defaultValue = "2") double defTeamTimeouts,
            @RequestParam double yardline,
            @RequestParam double down,
            @RequestParam double ydstogo) {

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "/Users/nikhilsankaran/PycharmProjects/nfl-analytics/.venv/bin/python",
                    "/Users/nikhilsankaran/PycharmProjects/nfl-analytics/predict_wp.py",
                    String.valueOf(scoreDiff),
                    String.valueOf(secondsRemaining),
                    String.valueOf(posTeamTimeouts),
                    String.valueOf(defTeamTimeouts),
                    String.valueOf(yardline),
                    String.valueOf(down),
                    String.valueOf(ydstogo)
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes()).trim();
            process.waitFor();

            Map<String, Object> result = mapper.readValue(output, Map.class);
            result.put("score_differential", scoreDiff);
            result.put("seconds_remaining", secondsRemaining);
            result.put("yardline", yardline);
            result.put("down", down);
            result.put("ydstogo", ydstogo);
            return result;

        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}