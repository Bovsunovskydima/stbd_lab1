package ua.lab.cafenet.persistence.repository;

import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.StatementRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ReviewRepository {

    private static final String COLUMNS = "id, order_id, branch, client, rating, comment";

    private final StatementRunner runner;

    public ReviewRepository(StatementRunner runner) {
        this.runner = runner;
    }

    public List<Rows.Review> all(Integer minRating) {
        if (minRating == null) {
            return runner.list("SELECT " + COLUMNS + " FROM v_reviews ORDER BY id DESC", ReviewRepository::review);
        }
        return runner.list("SELECT " + COLUMNS + " FROM v_reviews WHERE rating >= ? ORDER BY id DESC",
                ReviewRepository::review, minRating);
    }

    public long add(long orderId, short rating, String comment) {
        return runner.execId("""
                CALL sp_review_add(CAST(? AS BIGINT), CAST(? AS SMALLINT), CAST(? AS TEXT), NULL)""",
                orderId, rating, comment);
    }

    private static Rows.Review review(ResultSet rs) throws SQLException {
        return new Rows.Review(
                rs.getLong("id"),
                rs.getLong("order_id"),
                rs.getString("branch"),
                rs.getString("client"),
                rs.getShort("rating"),
                rs.getString("comment"));
    }
}
