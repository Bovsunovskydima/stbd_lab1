package ua.lab.cafenet.persistence.repository;

import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.StatementRunner;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class SupplyRepository {

    private static final String SUPPLIER_COLUMNS = "id, name, contact, purchases_count, last_purchase_at";
    private static final String PURCHASE_COLUMNS =
            "id, branch_id, branch, supplier_id, supplier, status, created_at, lines_count, total_cost";
    private static final String LINE_COLUMNS =
            "id, purchase_id, purchase_status, product_id, product, unit, quantity, unit_cost, line_cost";

    private final StatementRunner runner;

    public SupplyRepository(StatementRunner runner) {
        this.runner = runner;
    }

    public List<Rows.Supplier> suppliers() {
        return runner.list("SELECT " + SUPPLIER_COLUMNS + " FROM v_suppliers ORDER BY name",
                SupplyRepository::supplier);
    }

    public int addSupplier(String name, String contact) {
        return (int) runner.execId("CALL sp_supplier_add(CAST(? AS TEXT), CAST(? AS TEXT), NULL)", name, contact);
    }

    public List<Rows.Purchase> purchases(Integer branchId, String status) {
        if (branchId != null) {
            return runner.list("SELECT " + PURCHASE_COLUMNS
                            + " FROM v_purchases WHERE branch_id = ? ORDER BY created_at DESC",
                    SupplyRepository::purchase, branchId);
        }
        if (status != null && !status.isBlank()) {
            return runner.list("SELECT " + PURCHASE_COLUMNS
                            + " FROM v_purchases WHERE status = CAST(? AS VARCHAR) ORDER BY created_at DESC",
                    SupplyRepository::purchase, status.toUpperCase());
        }
        return runner.list("SELECT " + PURCHASE_COLUMNS + " FROM v_purchases ORDER BY created_at DESC",
                SupplyRepository::purchase);
    }

    public Optional<Rows.Purchase> purchaseById(long id) {
        return runner.single("SELECT " + PURCHASE_COLUMNS + " FROM v_purchases WHERE id = ?",
                SupplyRepository::purchase, id);
    }

    public List<Rows.PurchaseLine> lines(long purchaseId) {
        return runner.list("SELECT " + LINE_COLUMNS + " FROM v_purchase_lines WHERE purchase_id = ? ORDER BY id",
                SupplyRepository::line, purchaseId);
    }

    public long open(int branchId, int supplierId) {
        return runner.execId("CALL sp_purchase_open(CAST(? AS INT), CAST(? AS INT), NULL)", branchId, supplierId);
    }

    public long addLine(long purchaseId, long productId, BigDecimal quantity, BigDecimal unitCost) {
        return runner.execId("""
                CALL sp_purchase_line_add(CAST(? AS BIGINT), CAST(? AS BIGINT),
                                          CAST(? AS NUMERIC), CAST(? AS NUMERIC), NULL)""",
                purchaseId, productId, quantity, unitCost);
    }

    public void receive(long purchaseId) {
        runner.exec("CALL sp_purchase_receive(CAST(? AS BIGINT))", purchaseId);
    }

    public void cancel(long purchaseId) {
        runner.exec("CALL sp_purchase_cancel(CAST(? AS BIGINT))", purchaseId);
    }

    private static Rows.Supplier supplier(ResultSet rs) throws SQLException {
        return new Rows.Supplier(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("contact"),
                rs.getLong("purchases_count"),
                rs.getObject("last_purchase_at", LocalDateTime.class));
    }

    private static Rows.Purchase purchase(ResultSet rs) throws SQLException {
        return new Rows.Purchase(
                rs.getLong("id"),
                rs.getInt("branch_id"),
                rs.getString("branch"),
                rs.getInt("supplier_id"),
                rs.getString("supplier"),
                rs.getString("status"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getLong("lines_count"),
                rs.getBigDecimal("total_cost"));
    }

    private static Rows.PurchaseLine line(ResultSet rs) throws SQLException {
        return new Rows.PurchaseLine(
                rs.getLong("id"),
                rs.getLong("purchase_id"),
                rs.getString("purchase_status"),
                rs.getLong("product_id"),
                rs.getString("product"),
                rs.getString("unit"),
                rs.getBigDecimal("quantity"),
                rs.getBigDecimal("unit_cost"),
                rs.getBigDecimal("line_cost"));
    }
}
