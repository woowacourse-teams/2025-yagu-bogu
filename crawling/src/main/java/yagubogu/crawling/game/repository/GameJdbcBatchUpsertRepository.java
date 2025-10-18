package yagubogu.crawling.game.repository;

import yagubogu.crawling.game.dto.BatchResult;
import yagubogu.crawling.game.dto.GameUpsertRow;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

@Repository
@RequiredArgsConstructor
public class GameJdbcBatchUpsertRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 300;

    private static final String UPSERT_SQL = """
            INSERT INTO games
              (game_code, stadium_id, home_team_id, away_team_id, date, start_at,
               home_score, away_score, home_pitcher, away_pitcher, game_state)
            VALUES
              (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
              stadium_id = VALUES(stadium_id),
              home_team_id = VALUES(home_team_id),
              away_team_id = VALUES(away_team_id),
              date = VALUES(date),
              start_at = VALUES(start_at),
              home_score = VALUES(home_score),
              away_score = VALUES(away_score),
              home_pitcher = VALUES(home_pitcher),
              away_pitcher = VALUES(away_pitcher),
              game_state = VALUES(game_state)
            """;

    @Transactional
    public BatchResult batchUpsert(List<GameUpsertRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return BatchResult.empty();
        }

        StopWatch sw = new StopWatch("game-batch-upsert");
        sw.start("total");
        int success = 0;
        List<Integer> failedIdx = new ArrayList<>();

        for (int from = 0; from < rows.size(); from += BATCH_SIZE) {
            int to = Math.min(from + BATCH_SIZE, rows.size());
            List<GameUpsertRow> chunk = rows.subList(from, to);

            try {
                int[] results = jdbcTemplate.batchUpdate(UPSERT_SQL, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        GameUpsertRow r = chunk.get(i);
                        int p = 1;
                        ps.setString(p++, r.gameCode());
                        ps.setLong(p++, r.stadiumId());
                        ps.setLong(p++, r.homeTeamId());
                        ps.setLong(p++, r.awayTeamId());
                        ps.setObject(p++, r.date());
                        ps.setObject(p++, r.startAt());

                        if (r.homeScore() == null) {
                            ps.setNull(p++, Types.INTEGER);
                        } else {
                            ps.setInt(p++, r.homeScore());
                        }
                        if (r.awayScore() == null) {
                            ps.setNull(p++, Types.INTEGER);
                        } else {
                            ps.setInt(p++, r.awayScore());
                        }
                        if (r.homePitcher() == null) {
                            ps.setNull(p++, Types.VARCHAR);
                        } else {
                            ps.setString(p++, r.homePitcher());
                        }
                        if (r.awayPitcher() == null) {
                            ps.setNull(p++, Types.VARCHAR);
                        } else {
                            ps.setString(p++, r.awayPitcher());
                        }
                        ps.setString(p++, r.gameState());
                    }

                    @Override
                    public int getBatchSize() {
                        return chunk.size();
                    }
                });

                // 성공 카운트 합산 (성공 시 >=0 또는 SUCCESS_NO_INFO)
                for (int r : results) {
                    if (r == Statement.SUCCESS_NO_INFO || r >= 0) {
                        success++;
                    }
                }
            } catch (DataAccessException e) {
                // 배치 전체 실패 → 이 청크를 절반으로 나눠 재시도하면서 실패 인덱스 수집 (간단한 이진 분할 전략)
                failedIdx.addAll(debugAndCollectFailed(rows, from, to, e));
            }
        }

        sw.stop();
        return new BatchResult(success, failedIdx, sw.getTotalTimeMillis());
    }

    // 실패 원인 추적: 청크를 이진 분할하며 문제 행 인덱스 수집
    private List<Integer> debugAndCollectFailed(List<GameUpsertRow> rows, int from, int to, Exception cause) {
        List<Integer> failed = new ArrayList<>();
        // 작은 범위는 개별 실행으로 실패 행 정확히 집계
        if (to - from <= 4) {
            for (int i = from; i < to; i++) {
                try {
                    final int index = i;
                    jdbcTemplate.update(UPSERT_SQL, ps -> bindOne(ps, rows.get(index)));
                } catch (DataAccessException e) {
                    failed.add(i);
                }
            }
            return failed;
        }
        int mid = (from + to) / 2;
        try {
            batchRange(rows, from, mid);
        } catch (DataAccessException e) {
            failed.addAll(debugAndCollectFailed(rows, from, mid, e));
        }
        try {
            batchRange(rows, mid, to);
        } catch (DataAccessException e) {
            failed.addAll(debugAndCollectFailed(rows, mid, to, e));
        }
        return failed;
    }

    private void batchRange(List<GameUpsertRow> rows, int from, int to) {
        jdbcTemplate.batchUpdate(UPSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                bindOne(ps, rows.get(from + i));
            }

            @Override
            public int getBatchSize() {
                return to - from;
            }
        });
    }

    private void bindOne(PreparedStatement ps, GameUpsertRow r) throws SQLException {
        int p = 1;
        ps.setString(p++, r.gameCode());
        ps.setLong(p++, r.stadiumId());
        ps.setLong(p++, r.homeTeamId());
        ps.setLong(p++, r.awayTeamId());
        ps.setObject(p++, r.date());
        ps.setObject(p++, r.startAt());
        if (r.homeScore() == null) {
            ps.setNull(p++, Types.INTEGER);
        } else {
            ps.setInt(p++, r.homeScore());
        }
        if (r.awayScore() == null) {
            ps.setNull(p++, Types.INTEGER);
        } else {
            ps.setInt(p++, r.awayScore());
        }
        if (r.homePitcher() == null) {
            ps.setNull(p++, Types.VARCHAR);
        } else {
            ps.setString(p++, r.homePitcher());
        }
        if (r.awayPitcher() == null) {
            ps.setNull(p++, Types.VARCHAR);
        } else {
            ps.setString(p++, r.awayPitcher());
        }
        ps.setString(p++, r.gameState());
    }
}
