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

import static org.assertj.core.api.Assertions.assertThat;
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
        when(scoreService.leaderboard()).thenReturn(mockLeaderboard);

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
}
