package ua.lab.cafenet.persistence.repository;

import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.StatementRunner;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class BranchRepository {

    private static final String BRANCH_COLUMNS = "id, name, city, address, opens_at, closes_at, staff_count";
    private static final String POSITION_COLUMNS = "id, title, base_rate, staff_count";

    private final StatementRunner runner;

    public BranchRepository(StatementRunner runner) {
        this.runner = runner;
    }

    public List<Rows.Branch> all() {
        return runner.list("SELECT " + BRANCH_COLUMNS + " FROM v_branches ORDER BY city, name",
                BranchRepository::branch);
    }

    public Optional<Rows.Branch> byId(int id) {
        return runner.single("SELECT " + BRANCH_COLUMNS + " FROM v_branches WHERE id = ?",
                BranchRepository::branch, id);
    }

    public int add(String name, String city, String address, LocalTime opens, LocalTime closes) {
        return (int) runner.execId("""
                CALL sp_branch_add(CAST(? AS TEXT), CAST(? AS TEXT), CAST(? AS TEXT),
                                   CAST(? AS TIME), CAST(? AS TIME), NULL)""",
                name, city, address, opens, closes);
    }

    public void edit(int id, String name, String city, String address, LocalTime opens, LocalTime closes) {
        runner.exec("""
                CALL sp_branch_edit(CAST(? AS INT), CAST(? AS TEXT), CAST(? AS TEXT), CAST(? AS TEXT),
                                    CAST(? AS TIME), CAST(? AS TIME))""",
                id, name, city, address, opens, closes);
    }

    public List<Rows.Position> positions() {
        return runner.list("SELECT " + POSITION_COLUMNS + " FROM v_positions ORDER BY title",
                BranchRepository::position);
    }

    public int addPosition(String title, BigDecimal rate) {
        return (int) runner.execId("CALL sp_position_add(CAST(? AS TEXT), CAST(? AS NUMERIC), NULL)",
                title, rate);
    }

    private static Rows.Branch branch(ResultSet rs) throws SQLException {
        return new Rows.Branch(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("city"),
                rs.getString("address"),
                rs.getObject("opens_at", LocalTime.class),
                rs.getObject("closes_at", LocalTime.class),
                rs.getLong("staff_count"));
    }

    private static Rows.Position position(ResultSet rs) throws SQLException {
        return new Rows.Position(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getBigDecimal("base_rate"),
                rs.getLong("staff_count"));
    }
}
