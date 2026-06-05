package com.nflanalytics.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams")
@CrossOrigin(origins = "*")
public class TeamController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/{team}/tendencies")
    public Map<String, Object> getTeamTendencies(
            @PathVariable String team,
            @RequestParam(defaultValue = "2024") int season) {

        String sql = """
            SELECT
                COUNT(*) as total_plays,
                ROUND(AVG(pass_attempt)::numeric, 3) as pass_rate,
                ROUND(AVG(rush_attempt)::numeric, 3) as run_rate,
                ROUND(AVG(epa)::numeric, 3) as avg_epa,
                ROUND(AVG(CASE WHEN pass_attempt = 1 THEN epa END)::numeric, 3) as pass_epa,
                ROUND(AVG(CASE WHEN rush_attempt = 1 THEN epa END)::numeric, 3) as run_epa,
                ROUND(AVG(CASE WHEN down IN (1,2) THEN pass_attempt END)::numeric, 3) as early_down_pass_rate,
                ROUND(AVG(CASE WHEN down = 3 THEN pass_attempt END)::numeric, 3) as third_down_pass_rate,
                ROUND(AVG(CASE WHEN yardline_100 <= 20 THEN pass_attempt END)::numeric, 3) as redzone_pass_rate,
                ROUND(AVG(CASE WHEN down = 3 AND ydstogo <= 3 THEN pass_attempt END)::numeric, 3) as third_short_pass_rate,
                ROUND(AVG(CASE WHEN down = 3 AND ydstogo >= 7 THEN pass_attempt END)::numeric, 3) as third_long_pass_rate
            FROM plays
            WHERE posteam = ?
              AND season = ?
              AND play_type IN ('pass', 'run')
              AND down IS NOT NULL
        """;

        Map<String, Object> tendencies = jdbcTemplate.queryForMap(sql, team, season);
        tendencies.put("team", team);
        tendencies.put("season", season);
        return tendencies;
    }

    @GetMapping("/{team}/passing-leaders")
    public List<Map<String, Object>> getPassingLeaders(
            @PathVariable String team,
            @RequestParam(defaultValue = "2024") int season) {

        String sql = """
            SELECT
                passer_player_name as player,
                COUNT(*) as attempts,
                ROUND(SUM(epa)::numeric, 2) as total_epa,
                ROUND(AVG(epa)::numeric, 3) as avg_epa
            FROM plays
            WHERE posteam = ?
              AND season = ?
              AND play_type = 'pass'
              AND passer_player_name IS NOT NULL
            GROUP BY passer_player_name
            ORDER BY total_epa DESC
            LIMIT 5
        """;

        return jdbcTemplate.queryForList(sql, team, season);
    }
}