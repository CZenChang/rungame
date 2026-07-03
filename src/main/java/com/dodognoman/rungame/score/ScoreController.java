package com.dodognoman.rungame.score;

import com.dodognoman.rungame.authjwt.PassJwt;
import com.dodognoman.rungame.common.dto.ApiResponse;
import com.dodognoman.rungame.score.dto.LeaderboardEntry;
import com.dodognoman.rungame.score.dto.UpdateScoreRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/scores")
@Validated
public class ScoreController {

    private static final Logger log = LoggerFactory.getLogger(ScoreController.class);
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

    /** 排行榜。 */
    @GetMapping("/leaderboard")
    @PassJwt
    public ApiResponse<List<LeaderboardEntry>> leaderboard(
            @RequestParam(defaultValue = "10",required = false) @Min(1) int size ,
            @RequestParam(defaultValue = "0", required = false) int page) {
        return ApiResponse.ok(scoreService.leaderboard(size , page));
    }

    private Long currentUserId(HttpServletRequest req) {
        return Long.valueOf((String) req.getAttribute("userId"));
    }

    /** 排行榜，支援 SSE。 */
    @GetMapping(value = "/leaderboard/stream", produces = "text/event-stream")
    @PassJwt
    public SseEmitter leaderboardStream(@RequestParam(defaultValue = "10",required = false) @Min(1) int size,
                                        @RequestParam(defaultValue = "0", required = false) int page) {
        SseEmitter emitter = new SseEmitter(0L);
        // 因為要先回應給前端 emitter，才能建立 SSE 連線，所以要在另一個執行緒傳送資料
        Thread.startVirtualThread(() -> {
            try {
                scoreService.leaderboard(size, page).forEach(entry -> {
                    try {
                        emitter.send(entry);
                        // 注意：如果用戶端網路緩慢或斷線，emitter.send 會拋出 IOException 或是直接「阻塞」。
                        // 在虛擬執行緒模型下，「阻塞」本身就是一種天然、低成本的背壓（Backpressure）機制。
                        // 這會暫停 JPA 繼續 fetch 下一批資料，直到緩衝區空出。
                    } catch (Exception _) {
                        // 當傳送發生異常（例如客戶端斷線），拋出 RuntimeException 來中止 Stream 迴圈
                        log.warn("Failed to send leaderboard entry to client, stopping stream");
                    }
                });
                emitter.send("{}"); // 傳送一個空的 JSON 物件，表示 以無資料, 以免前端重連(sse 預設行為)
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }


}
