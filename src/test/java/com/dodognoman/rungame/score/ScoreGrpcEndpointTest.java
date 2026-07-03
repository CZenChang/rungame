package com.dodognoman.rungame.score;

import com.dodognoman.rungame.score.dto.LeaderboardEntry;
import com.dodognoman.rungame.score.grpc.LeaderboardRequest;
import com.dodognoman.rungame.score.grpc.LeaderboardResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreGrpcEndpointTest {

    @Mock
    private ScoreService scoreService;

    @InjectMocks
    private ScoreGrpcEndpoint scoreGrpcEndpoint;

    @Test
    void getLeaderboard_shouldReturnLeaderboardEntries() {
        // 1. 準備 Mock 測試資料
        List<LeaderboardEntry> mockLeaderboard = List.of(
                new LeaderboardEntry("player1", 150),
                new LeaderboardEntry("player2", 120)
        );
        when(scoreService.leaderboard(anyInt(), anyInt())).thenReturn(mockLeaderboard);

        // 2. 準備 Request 與 Mock StreamObserver
        LeaderboardRequest request = LeaderboardRequest.newBuilder().build();
        @SuppressWarnings("unchecked")
        StreamObserver<LeaderboardResponse> responseObserver = mock(StreamObserver.class);

        // 3. 執行測試
        scoreGrpcEndpoint.getLeaderboard(request, responseObserver);

        // 4. 驗證
        ArgumentCaptor<LeaderboardResponse> responseCaptor = ArgumentCaptor.forClass(LeaderboardResponse.class);
        verify(responseObserver, times(1)).onNext(responseCaptor.capture());
        verify(responseObserver, times(1)).onCompleted();
        verify(responseObserver, never()).onError(any());

        LeaderboardResponse actualResponse = responseCaptor.getValue();
        assertThat(actualResponse.getEntriesList()).hasSize(2);
        assertThat(actualResponse.getEntries(0).getUsername()).isEqualTo("player1");
        assertThat(actualResponse.getEntries(0).getScore()).isEqualTo(150);
        assertThat(actualResponse.getEntries(1).getUsername()).isEqualTo("player2");
        assertThat(actualResponse.getEntries(1).getScore()).isEqualTo(120);
    }
}
