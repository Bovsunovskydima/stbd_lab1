package ua.lab.cafenet.persistence.repository;

import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.StatementRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ProductRepository {

    private static final String COLUMNS = "id, name, unit, modified_at, modified_by";

    private final StatementRunner runner;

    public ProductRepository(StatementRunner runner) {
        this.runner = runner;
    }

    public List<Rows.Product> search(String name) {
        if (name == null || name.isBlank()) {
            return runner.list("SELECT " + COLUMNS + " FROM v_products ORDER BY name", ProductRepository::product);
        }
        return runner.list("""
                SELECT %s FROM v_products
                WHERE name ILIKE '%%' || CAST(? AS TEXT) || '%%'
                ORDER BY name""".formatted(COLUMNS), ProductRepository::product, name.trim());
    }

    public Optional<Rows.Product> byId(long id) {
        return runner.single("SELECT " + COLUMNS + " FROM v_products WHERE id = ?", ProductRepository::product, id);
    }

    public long add(String name, String unit) {
        return runner.execId("CALL sp_product_add(CAST(? AS TEXT), CAST(? AS TEXT), NULL)", name, unit);
    }

    public void edit(long id, String name, String unit) {
        runner.exec("CALL sp_product_edit(CAST(? AS BIGINT), CAST(? AS TEXT), CAST(? AS TEXT))", id, name, unit);
    }

    public void remove(long id) {
        runner.exec("CALL sp_product_drop(CAST(? AS BIGINT))", id);
    }

    public void restore(long id) {
        runner.exec("CALL sp_product_restore(CAST(? AS BIGINT))", id);
    }

    private static Rows.Product product(ResultSet rs) throws SQLException {
        return new Rows.Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("unit"),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getObject("modified_by", Long.class));
    }
}
