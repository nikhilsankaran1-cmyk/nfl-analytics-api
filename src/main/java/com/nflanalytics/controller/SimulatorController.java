package com.nflanalytics.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simulator")
@CrossOrigin(origins = "*")
public class SimulatorController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/projections")
    public List<Map<String, Object>> getProjections(
            @RequestParam(defaultValue = "2026") int season) {

        String sql = """
            SELECT team, conference, division, vegas_wins,
                   avg_wins, playoff_pct, superbowl_pct
            FROM season_projections
            WHERE season = ?
            ORDER BY superbowl_pct DESC
        """;

        return jdbcTemplate.queryForList(sql, season);
    }

    @GetMapping("/projections/conference")
    public List<Map<String, Object>> getByConference(
            @RequestParam(defaultValue = "2026") int season,
            @RequestParam String conference) {

        String sql = """
            SELECT team, division, vegas_wins,
                   avg_wins, playoff_pct, superbowl_pct
            FROM season_projections
            WHERE season = ? AND conference = ?
            ORDER BY superbowl_pct DESC
        """;

        return jdbcTemplate.queryForList(sql, season, conference);
    }
}