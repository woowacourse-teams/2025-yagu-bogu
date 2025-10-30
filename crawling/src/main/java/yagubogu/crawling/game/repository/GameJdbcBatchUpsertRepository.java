package yagubogu.crawling.game.repository;

import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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
import yagubogu.crawling.game.dto.ScoreBoardData;
import yagubogu.crawling.game.dto.ScoreBoardIds;

@Repository
@RequiredArgsConstructor
public class GameJdbcBatchUpsertRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 300;

    private static final String INSERT_SCOREBOARD_SQL = """
            INSERT INTO score_boards
              (runs, hits, errors, bases_on_balls, inning_scores)
            VALUES
              (?, ?, ?, ?, ?)
            """;

    private static final String UPSERT_GAME_SQL = """
            INSERT INTO games
              (game_code, stadium_id, home_team_id, away_team_id, date, start_at,
               home_score, away_score, home_pitcher, away_pitcher, game_state,
               home_score_board_id, away_score_board_id)
            VALUES
              (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
              game_state = VALUES(game_state),
              home_score_board_id = VALUES(home_score_board_id),
              away_score_board_id = VALUES(away_score_board_id)
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
                // 1. ScoreBoard 먼저 INSERT (ID 생성)
                Map<Integer, ScoreBoardIds> scoreBoardIdMap = batchInsertScoreBoards(chunk);

                // 2. Game UPSERT (ScoreBoard ID 참조)
                int[] results = jdbcTemplate.batchUpdate(UPSERT_GAME_SQL,
                        new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                GameUpsertRow r = chunk.get(i);
                                ScoreBoardIds ids = scoreBoardIdMap.get(i);
                                bindGameWithScoreBoard(ps, r, ids);
                            }

                            @Override
                            public int getBatchSize() {
                                return chunk.size();
                            }
                        });

                // 성공 카운트 합산
                for (int r : results) {
                    if (r == Statement.SUCCESS_NO_INFO || r >= 0) {
                        success++;
                    }
                }
            } catch (DataAccessException e) {
                failedIdx.addAll(debugAndCollectFailed(rows, from, to, e));
            }
        }

        sw.stop();
        return new BatchResult(success, failedIdx, sw.getTotalTimeMillis());
    }

    /**
     * ScoreBoard Batch INSERT 후 생성된 ID 반환
     */
    private Map<Integer, ScoreBoardIds> batchInsertScoreBoards(List<GameUpsertRow> chunk) {
        Map<Integer, ScoreBoardIds> idMap = new HashMap<>();

        // Home ScoreBoard Batch Insert
        List<Long> homeIds = batchInsertScoreBoardsForTeam(chunk, true);

        // Away ScoreBoard Batch Insert
        List<Long> awayIds = batchInsertScoreBoardsForTeam(chunk, false);

        // Map 생성
        for (int i = 0; i < chunk.size(); i++) {
            Long homeId = homeIds.get(i);
            Long awayId = awayIds.get(i);
            idMap.put(i, new ScoreBoardIds(homeId, awayId));
        }

        return idMap;
    }

    /**
     * Home 또는 Away ScoreBoard만 Batch Insert
     */
    private List<Long> batchInsertScoreBoardsForTeam(List<GameUpsertRow> chunk, boolean isHome) {
        List<Long> ids = new ArrayList<>();

        // KeyHolder로 생성된 ID 수집
        for (GameUpsertRow row : chunk) {
            ScoreBoardData data = isHome ? row.homeScoreBoard() : row.awayScoreBoard();

            if (data == null) {
                ids.add(null);
                continue;
            }

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        INSERT_SCOREBOARD_SQL,
                        Statement.RETURN_GENERATED_KEYS
                );
                bindScoreBoard(ps, data);
                return ps;
            }, keyHolder);

            Long generatedId = keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
            ids.add(generatedId);
        }

        return ids;
    }

    /**
     * ScoreBoard 바인딩
     */
    private void bindScoreBoard(PreparedStatement ps, ScoreBoardData data)
            throws SQLException {
        int p = 1;
        ps.setInt(p++, data.runs() != null ? data.runs() : 0);
        ps.setInt(p++, data.hits() != null ? data.hits() : 0);
        ps.setInt(p++, data.errors() != null ? data.errors() : 0);
        ps.setInt(p++, data.basesOnBalls() != null ? data.basesOnBalls() : 0);
        ps.setString(p++, data.inningScores() != null ? data.inningScores() : "");
    }

    /**
     * Game + ScoreBoard ID 바인딩
     */
    private void bindGameWithScoreBoard(PreparedStatement ps, GameUpsertRow r, ScoreBoardIds ids)
            throws SQLException {
        int p = 1;
        ps.setString(p++, r.gameCode());
        ps.setLong(p++, r.stadiumId());
        ps.setLong(p++, r.homeTeamId());
        ps.setLong(p++, r.awayTeamId());
        ps.setObject(p++, r.date());
        ps.setObject(p++, r.startAt());

        // Nullable 필드 처리
        setNullableInt(ps, p++, r.homeScore());
        setNullableInt(ps, p++, r.awayScore());
        setNullableString(ps, p++, r.homePitcher());
        setNullableString(ps, p++, r.awayPitcher());

        ps.setString(p++, r.gameState());

        // ScoreBoard ID
        setNullableLong(ps, p++, ids.homeScoreBoardId());
        setNullableLong(ps, p++, ids.awayScoreBoardId());
    }

    // ==================== 유틸리티 메서드 ====================

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }

    // ==================== 실패 처리 (기존 로직 유지) ====================

    private List<Integer> debugAndCollectFailed(List<GameUpsertRow> rows, int from, int to, Exception cause) {
        List<Integer> failed = new ArrayList<>();

        if (to - from <= 4) {
            for (int i = from; i < to; i++) {
                try {
                    final int index = i;
                    upsertSingle(rows.get(index));
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
        List<GameUpsertRow> chunk = rows.subList(from, to);
        Map<Integer, ScoreBoardIds> idMap = batchInsertScoreBoards(chunk);

        jdbcTemplate.batchUpdate(UPSERT_GAME_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                bindGameWithScoreBoard(ps, chunk.get(i), idMap.get(i));
            }

            @Override
            public int getBatchSize() {
                return chunk.size();
            }
        });
    }

    private void upsertSingle(GameUpsertRow row) {
        // ScoreBoard 먼저 INSERT
        Long homeScoreBoardId = insertScoreBoardSingle(row.homeScoreBoard());
        Long awayScoreBoardId = insertScoreBoardSingle(row.awayScoreBoard());

        // Game UPSERT
        jdbcTemplate.update(UPSERT_GAME_SQL, ps ->
                bindGameWithScoreBoard(ps, row, new ScoreBoardIds(homeScoreBoardId, awayScoreBoardId))
        );
    }

    private Long insertScoreBoardSingle(ScoreBoardData data) {
        if (data == null) {
            return null;
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_SCOREBOARD_SQL,
                    Statement.RETURN_GENERATED_KEYS
            );
            bindScoreBoard(ps, data);
            return ps;
        }, keyHolder);

        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }
}
