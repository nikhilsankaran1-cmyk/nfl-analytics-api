package com.nflanalytics.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "*")
public class GameController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public List<Map<String, Object>> getGames(
            @RequestParam(defaultValue = "2024") int season,
            @RequestParam(required = false) Integer week) {

        String sql = week != null ? """
            SELECT DISTINCT game_id, home_team, away_team, week, season,
                home_score, away_score
            FROM plays
            WHERE season = ? AND week = ?
            ORDER BY week, game_id
        """ : """
            SELECT DISTINCT game_id, home_team, away_team, week, season,
                home_score, away_score
            FROM plays
            WHERE season = ?
            ORDER BY week, game_id
        """;

        return week != null
                ? jdbcTemplate.queryForList(sql, season, week)
                : jdbcTemplate.queryForList(sql, season);
    }

    @GetMapping("/win-probability")
    public List<Map<String, Object>> getGameWinProbability(
            @RequestParam String gameId) {

        String sql = """
            SELECT
                qtr,
                game_seconds_remaining,
                score_differential,
                posteam,
                home_team,
                away_team,
                posteam_score,
                defteam_score,
                play_type,
                wp
            FROM plays
            WHERE game_id = ?
              AND play_type IN ('pass', 'run')
              AND game_seconds_remaining IS NOT NULL
            ORDER BY game_seconds_remaining DESC
        """;

        return jdbcTemplate.queryForList(sql, gameId);
    }

    @GetMapping("/key-plays")
    public List<Map<String, Object>> getKeyPlays(@RequestParam String gameId) {

        String sql = """
            SELECT
                qtr,
                game_seconds_remaining,
                posteam,
                play_type,
                yards_gained,
                epa,
                touchdown,
                interception,
                fumble_lost
            FROM plays
            WHERE game_id = ?
              AND ABS(epa) > 2.0
              AND play_type IN ('pass', 'run')
            ORDER BY ABS(epa) DESC
            LIMIT 10
        """;

        return jdbcTemplate.queryForList(sql, gameId);
    }
}