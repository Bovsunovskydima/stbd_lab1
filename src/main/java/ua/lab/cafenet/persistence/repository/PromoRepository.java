package ua.lab.cafenet.persistence.repository;

import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.StatementRunner;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PromoRepository {

    private static final String COLUMNS = "id, code, discount_percent, valid_to, is_valid, modified_at, modified_by";

    private final StatementRunner runner;

    public PromoRepository(StatementRunner runner) {
        this.runner = runner;
    }

    public List<Rows.Promo> all(Boolean onlyValid) {
        if (Boolean.TRUE.equals(onlyValid)) {
            return runner.list("SELECT " + COLUMNS + " FROM v_promos WHERE is_valid ORDER BY code",
                    PromoRepository::promo);
        }
        return runner.list("SELECT " + COLUMNS + " FROM v_promos ORDER BY code", PromoRepository::promo);
    }

    public Optional<Rows.Promo> byId(int id) {
        return runner.single("SELECT " + COLUMNS + " FROM v_promos WHERE id = ?", PromoRepository::promo, id);
    }

    public int add(String code, BigDecimal percent, LocalDate validTo) {
        return (int) runner.execId("CALL sp_promo_add(CAST(? AS TEXT), CAST(? AS NUMERIC), CAST(? AS DATE), NULL)",
                code, percent, validTo);
    }

    public void edit(int id, BigDecimal percent, LocalDate validTo) {
        runner.exec("CALL sp_promo_edit(CAST(? AS INT), CAST(? AS NUMERIC), CAST(? AS DATE))", id, percent, validTo);
    }

    public void remove(int id) {
        runner.exec("CALL sp_promo_drop(CAST(? AS INT))", id);
    }

    public void restore(int id) {
        runner.exec("CALL sp_promo_restore(CAST(? AS INT))", id);
    }

    private static Rows.Promo promo(ResultSet rs) throws SQLException {
        return new Rows.Promo(
                rs.getInt("id"),
                rs.getString("code"),
                rs.getBigDecimal("discount_percent"),
                rs.getObject("valid_to", LocalDate.class),
                rs.getBoolean("is_valid"),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getObject("modified_by", Long.class));
    }
}
