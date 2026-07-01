package com.dodognoman.rungame.score;

import com.dodognoman.rungame.score.grpc.LeaderboardRequest;
import com.dodognoman.rungame.score.grpc.LeaderboardResponse;
import com.dodognoman.rungame.score.grpc.ScoreServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;

/**
 * 分數的 gRPC 端點，與 {@link ScoreController}（REST）共用同一套 {@link ScoreService} 業務邏輯。
 * 由 Spring gRPC starter 自動註冊到 gRPC server（預設埠 9090）。
 */
@GrpcService
public class ScoreGrpcEndpoint extends ScoreServiceGrpc.ScoreServiceImplBase {

    private final ScoreService scoreService;

    public ScoreGrpcEndpoint(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    /** 取得排行榜前十名，對應 REST 的 GET /api/scores/leaderboard。 */
    @Override
    public void getLeaderboard(LeaderboardRequest request,
                               StreamObserver<LeaderboardResponse> responseObserver) {
        List<com.dodognoman.rungame.score.grpc.LeaderboardEntry> entries = scoreService.leaderboard().stream()
                .map(e -> com.dodognoman.rungame.score.grpc.LeaderboardEntry.newBuilder()
                        .setUsername(e.username())
                        .setScore(e.score())
                        .build())
                .toList();

        LeaderboardResponse response = LeaderboardResponse.newBuilder()
                .addAllEntries(entries)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
