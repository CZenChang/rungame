package com.dodognoman.rungame.score;

import com.dodognoman.rungame.common.dto.ApiResponse;
import com.dodognoman.rungame.score.dto.LeaderboardEntry;
import com.dodognoman.rungame.score.dto.UpdateScoreRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    private final ScoreService scoreService;

    public ScoreController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    /** 更新分數，回傳更新後的分數。 */
    @PutMapping("/me")
    public ApiResponse<Integer> updateMyScore(@Valid @RequestBody UpdateScoreRequest req,
                                              HttpServletRequest httpRequest) {
        return ApiResponse.ok(scoreService.updateScore(currentUserId(httpRequest), req.score()));
    }

    /** 查詢自己的分數。 */
    @GetMapping("/me")
    public ApiResponse<Integer> getMyScore(HttpServletRequest httpRequest) {
        return ApiResponse.ok(scoreService.getScore(currentUserId(httpRequest)));
    }

    /** 排行榜前十名。 */
    @GetMapping("/leaderboard")
    public ApiResponse<List<LeaderboardEntry>> leaderboard() {
        return ApiResponse.ok(scoreService.leaderboard());
    }

    private Long currentUserId(HttpServletRequest req) {
        return Long.valueOf((String) req.getAttribute("userId"));
    }
}
