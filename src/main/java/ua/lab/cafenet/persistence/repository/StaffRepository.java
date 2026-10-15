package ua.lab.cafenet.persistence.repository;

import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.StatementRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StaffRepository {

    private static final String COLUMNS = """
            id, branch_id, branch, position_id, position, full_name, phone, hired_on,
            modified_at, modified_by, modified_by_name""";

    private static final String SHIFT_COLUMNS = "id, staff_id, staff_name, branch, starts_at, ends_at, hours";

    private final StatementRunner runner;

    public StaffRepository(StatementRunner runner) {
        this.runner = runner;
    }

    public List<Rows.Staff> search(Integer branchId, String name) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM v_staff WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (branchId != null) {
            sql.append(" AND branch_id = ?");
            args.add(branchId);
        }
        if (name != null && !name.isBlank()) {
            sql.append(" AND full_name ILIKE '%' || CAST(? AS TEXT) || '%'");
            args.add(name.trim());
        }
        sql.append(" ORDER BY branch, full_name");
        return runner.list(sql.toString(), StaffRepository::staff, args.toArray());
    }

    public Optional<Rows.Staff> byId(long id) {
        return runner.single("SELECT " + COLUMNS + " FROM v_staff WHERE id = ?", StaffRepository::staff, id);
    }

    public long hire(int branchId, int positionId, String fullName, String phone, LocalDate hiredOn) {
        return runner.execId("""
                CALL sp_staff_hire(CAST(? AS INT), CAST(? AS INT), CAST(? AS TEXT),
                                   CAST(? AS TEXT), CAST(? AS DATE), NULL)""",
                branchId, positionId, fullName, phone, hiredOn);
    }

    public void edit(long id, int positionId, String phone) {
        runner.exec("CALL sp_staff_edit(CAST(? AS BIGINT), CAST(? AS INT), CAST(? AS TEXT))",
                id, positionId, phone);
    }

    public void move(long id, int branchId) {
        runner.exec("CALL sp_staff_move(CAST(? AS BIGINT), CAST(? AS INT))", id, branchId);
    }

    public void dismiss(long id) {
        runner.exec("CALL sp_staff_drop(CAST(? AS BIGINT))", id);
    }

    public void restore(long id) {
        runner.exec("CALL sp_staff_restore(CAST(? AS BIGINT))", id);
    }

    public List<Rows.Shift> shifts(Long staffId) {
        if (staffId == null) {
            return runner.list("SELECT " + SHIFT_COLUMNS + " FROM v_shifts ORDER BY starts_at DESC",
                    StaffRepository::shift);
        }
        return runner.list("SELECT " + SHIFT_COLUMNS + " FROM v_shifts WHERE staff_id = ? ORDER BY starts_at DESC",
                StaffRepository::shift, staffId);
    }

    public long planShift(long staffId, LocalDateTime from, LocalDateTime to) {
        return runner.execId("""
                CALL sp_shift_add(CAST(? AS BIGINT), CAST(? AS TIMESTAMP), CAST(? AS TIMESTAMP), NULL)""",
                staffId, from, to);
    }

    public boolean onShift(long staffId, LocalDateTime from, LocalDateTime to) {
        return runner.scalar("""
                SELECT fn_staff_on_shift(CAST(? AS BIGINT), CAST(? AS TIMESTAMP), CAST(? AS TIMESTAMP))""",
                rs -> rs.getBoolean(1), staffId, from, to);
    }

    private static Rows.Staff staff(ResultSet rs) throws SQLException {
        return new Rows.Staff(
                rs.getLong("id"),
                rs.getInt("branch_id"),
                rs.getString("branch"),
                rs.getInt("position_id"),
                rs.getString("position"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getObject("hired_on", LocalDate.class),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getObject("modified_by", Long.class),
                rs.getString("modified_by_name"));
    }

    private static Rows.Shift shift(ResultSet rs) throws SQLException {
        return new Rows.Shift(
                rs.getLong("id"),
                rs.getLong("staff_id"),
                rs.getString("staff_name"),
                rs.getString("branch"),
                rs.getObject("starts_at", LocalDateTime.class),
                rs.getObject("ends_at", LocalDateTime.class),
                rs.getBigDecimal("hours"));
    }
}
