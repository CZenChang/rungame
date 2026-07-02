package com.dodognoman.rungame.score;

import com.dodognoman.rungame.score.grpc.LeaderboardEntry;
import com.dodognoman.rungame.score.grpc.LeaderboardRequest;
import com.dodognoman.rungame.score.grpc.LeaderboardResponse;
import com.dodognoman.rungame.score.grpc.ScoreServiceGrpc;
import io.grpc.stub.ServerCallStreamObserver;
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

    @Override
    public void getLeaderboardStream(LeaderboardRequest request, StreamObserver<LeaderboardEntry> responseObserver) {

        // 真要做stream , 這邊也不該是一次性回傳所有資料, 而是要一筆一筆的回傳, 這邊先用簡單的方式實作
        // 如果是from db 可以改用Stream(JPA有實現)
        java.util.Iterator<LeaderboardEntry> iterator = scoreService.leaderboard().stream()
                .map(e -> LeaderboardEntry.newBuilder()
                        .setUsername(e.username())
                        .setScore(e.score())
                        .build())
                .iterator();

        ServerCallStreamObserver<LeaderboardEntry> servercall = (ServerCallStreamObserver<LeaderboardEntry>) responseObserver;

        // OnReady 代表網路and client 都準備好了，才開始傳送資料
        servercall.setOnReadyHandler(() -> {
            while (servercall.isReady() && iterator.hasNext()) {
                servercall.onNext(iterator.next());
            }

            if (!iterator.hasNext()) {
                servercall.onCompleted();
            }
        });
    }
}
