package com.dodognoman.rungame.score;

import com.dodognoman.rungame.score.dto.LeaderboardEntry;
import com.dodognoman.rungame.score.grpc.LeaderboardRequest;
import com.dodognoman.rungame.score.grpc.LeaderboardResponse;
import com.dodognoman.rungame.score.grpc.ScoreServiceGrpc;
import io.grpc.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

/**
 * 主要測試 grpc 連線 ,  所以一個方法就可以了
 */
@SpringBootTest(properties = {
        "spring.cloud.gcp.core.enabled=false",
        "spring.cloud.gcp.logging.enabled=false",
        "spring.datasource.url=jdbc:tc:postgresql:17-alpine:///rungame",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
        "spring.main.lazy-initialization=true"
}
)
class ScoreGrpcEndpointIT {

    @MockitoBean
    private ScoreService scoreService;

    // Mock 住 UserRepository 解決 UserController 的依賴注入問題
    @MockitoBean
    private com.dodognoman.rungame.user.repo.UserRepository userRepository;

    @Autowired
    private GrpcChannelFactory channelFactory;

    @Test
    void getLeaderboard_viaGrpcChannel() {
        // 準備 mock 資料
        List<LeaderboardEntry> mockLeaderboard = List.of(
                new LeaderboardEntry("playerA", 500)
        );
        when(scoreService.leaderboard(anyInt(), anyInt())).thenReturn(mockLeaderboard);

        // 建立 gRPC Client Stub
        Channel channel = channelFactory.createChannel("127.0.0.1:9090");
        ScoreServiceGrpc.ScoreServiceBlockingStub stub = ScoreServiceGrpc.newBlockingStub(channel);

        // 發送真實的 gRPC 請求
        LeaderboardResponse response = stub.getLeaderboard(LeaderboardRequest.newBuilder().build());

        // 驗證
        assertThat(response.getEntriesList()).hasSize(1);
        assertThat(response.getEntries(0).getUsername()).isEqualTo("playerA");
        assertThat(response.getEntries(0).getScore()).isEqualTo(500);
    }

    @Test
    void getLeaderboardStream_viaGrpcChannel() throws InterruptedException {
        // 準備 mock 資料
        List<LeaderboardEntry> mockLeaderboard = List.of(
                new LeaderboardEntry("playerA", 500)
        );
        when(scoreService.leaderboard(anyInt(), anyInt())).thenReturn(mockLeaderboard);

        // 建立 gRPC Client Stub
        Channel channel = channelFactory.createChannel("127.0.0.1:9090");
        ScoreServiceGrpc.ScoreServiceStub stub = ScoreServiceGrpc.newStub(channel);

        // 準備接收資料的容器與鎖
        List<com.dodognoman.rungame.score.grpc.LeaderboardEntry> receivedEntries = new java.util.concurrent.CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        // 發送非同步串流請求
        stub.getLeaderboardStream(LeaderboardRequest.newBuilder().build(), new io.grpc.stub.StreamObserver<>() {
            @Override
            public void onNext(com.dodognoman.rungame.score.grpc.LeaderboardEntry value) {
                receivedEntries.add(value);
            }

            @Override
            public void onError(Throwable t) {
                // 讓測試執行緒啟動，從wait 狀態中醒來
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        // 讓測試執行緒等待串流結果，最多等 5 秒
        boolean completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        // 驗證
        assertThat(receivedEntries).hasSize(1);
        assertThat(receivedEntries.get(0).getUsername()).isEqualTo("playerA");
        assertThat(receivedEntries.get(0).getScore()).isEqualTo(500);
    }

}
