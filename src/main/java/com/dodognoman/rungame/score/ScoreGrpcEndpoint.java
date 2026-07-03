package com.dodognoman.rungame.score;

import com.dodognoman.rungame.score.grpc.LeaderboardEntry;
import com.dodognoman.rungame.score.grpc.LeaderboardRequest;
import com.dodognoman.rungame.score.grpc.LeaderboardResponse;
import com.dodognoman.rungame.score.grpc.ScoreServiceGrpc;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Iterator;
import java.util.List;

/**
 * 分數的 gRPC 端點，與 {@link ScoreController}（REST）共用同一套 {@link ScoreService} 業務邏輯。
 * 由 Spring gRPC starter 自動註冊到 gRPC server（預設埠 9090）。
 */
@GrpcService
public class ScoreGrpcEndpoint extends ScoreServiceGrpc.ScoreServiceImplBase {

    /** request 未帶 size（proto3 int32 預設 0）時套用的每頁筆數。 */
    private static final int DEFAULT_SIZE = 10;

    private final ScoreService scoreService;

    public ScoreGrpcEndpoint(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    /** 取得排行榜（支援分頁），對應 REST 的 GET /api/scores/leaderboard。 */
    @Override
    public void getLeaderboard(LeaderboardRequest request,
                               StreamObserver<LeaderboardResponse> responseObserver) {
        List<LeaderboardEntry> entries = scoreService.leaderboard(sizeOf(request), request.getPage()).stream()
                .map(e -> LeaderboardEntry.newBuilder()
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

        // 資料先在 service 的 @Transactional 內撈成記憶體 list——不可把 JPA Stream 帶進
        // onReadyHandler，因為 handler 是非同步觸發，屆時交易/Session 已關閉。
        Iterator<LeaderboardEntry> iterator = scoreService.leaderboard(sizeOf(request), request.getPage()).stream()
                .map(e -> LeaderboardEntry.newBuilder()
                        .setUsername(e.username())
                        .setScore(e.score())
                        .build())
                .iterator();

        ServerCallStreamObserver<LeaderboardEntry> servercall = (ServerCallStreamObserver<LeaderboardEntry>) responseObserver;

        // OnReady 代表傳送 buffer 有空間才送；滿了就跳出，等下次 onReady 再續送（背壓）。
        servercall.setOnReadyHandler(() -> {
            while (servercall.isReady() && iterator.hasNext()) {
                servercall.onNext(iterator.next());
            }
            // 全部送完才 complete；中途 buffer 滿而跳出時 iterator 仍有資料，不會提前結束。
            if (!iterator.hasNext()) {
                servercall.onCompleted();
            }
        });
    }

    /** request 未帶 size 時回傳預設值，避免 PageRequest 因 size=0 拋例外。 */
    private static int sizeOf(LeaderboardRequest request) {
        return request.getSize() > 0 ? request.getSize() : DEFAULT_SIZE;
    }
}
