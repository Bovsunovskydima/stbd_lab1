package ua.lab.cafenet.persistence.repository;

import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.StatementRunner;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalesRepository {

    private static final String ORDER_COLUMNS = """
            id, branch_id, branch, client_id, client, cashier_id, cashier, promo_id, promo_code,
            channel, status, total, created_at, modified_at, modified_by, lines_count""";

    private static final String LINE_COLUMNS = """
            id, order_id, order_status, menu_item_id, menu_item, section, quantity, price_at_sale, line_total""";

    private static final String PAYMENT_COLUMNS = "id, order_id, order_status, method, amount, paid_at";

    private final StatementRunner runner;

    public SalesRepository(StatementRunner runner) {
        this.runner = runner;
    }

    public List<Rows.Order> search(Integer branchId, String status, String channel) {
        StringBuilder sql = new StringBuilder("SELECT " + ORDER_COLUMNS + " FROM v_orders WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (branchId != null) {
            sql.append(" AND branch_id = ?");
            args.add(branchId);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND status = CAST(? AS VARCHAR)");
            args.add(status.toUpperCase());
        }
        if (channel != null && !channel.isBlank()) {
            sql.append(" AND channel = CAST(? AS VARCHAR)");
            args.add(channel.toUpperCase());
        }
        sql.append(" ORDER BY created_at DESC");
        return runner.list(sql.toString(), SalesRepository::order, args.toArray());
    }

    public Optional<Rows.Order> byId(long id) {
        return runner.single("SELECT " + ORDER_COLUMNS + " FROM v_orders WHERE id = ?", SalesRepository::order, id);
    }

    public List<Rows.OrderLine> lines(long orderId) {
        return runner.list("SELECT " + LINE_COLUMNS + " FROM v_order_lines WHERE order_id = ? ORDER BY id",
                SalesRepository::line, orderId);
    }

    public List<Rows.Payment> payments(long orderId) {
        return runner.list("SELECT " + PAYMENT_COLUMNS + " FROM v_payments WHERE order_id = ? ORDER BY paid_at",
                SalesRepository::payment, orderId);
    }

    public BigDecimal amount(long orderId) {
        return runner.scalar("SELECT fn_order_amount(CAST(? AS BIGINT))", rs -> rs.getBigDecimal(1), orderId);
    }

    public long open(int branchId, Long clientId, long cashierId, String channel) {
        return runner.execId("""
                CALL sp_order_open(CAST(? AS INT), CAST(? AS BIGINT), CAST(? AS BIGINT),
                                   CAST(? AS TEXT), NULL)""",
                branchId, clientId, cashierId, channel);
    }

    public long addLine(long orderId, long menuItemId, int quantity) {
        return runner.execId("CALL sp_order_line_add(CAST(? AS BIGINT), CAST(? AS BIGINT), CAST(? AS INT), NULL)",
                orderId, menuItemId, quantity);
    }

    public void dropLine(long lineId) {
        runner.exec("CALL sp_order_line_drop(CAST(? AS BIGINT))", lineId);
    }

    public void applyPromo(long orderId, String code) {
        runner.exec("CALL sp_order_promo(CAST(? AS BIGINT), CAST(? AS TEXT))", orderId, code);
    }

    public long pay(long orderId, String method) {
        return runner.execId("CALL sp_order_pay(CAST(? AS BIGINT), CAST(? AS TEXT), NULL)", orderId, method);
    }

    public void close(long orderId) {
        runner.exec("CALL sp_order_close(CAST(? AS BIGINT))", orderId);
    }

    public void cancel(long orderId) {
        runner.exec("CALL sp_order_cancel(CAST(? AS BIGINT))", orderId);
    }

    private static Rows.Order order(ResultSet rs) throws SQLException {
        return new Rows.Order(
                rs.getLong("id"),
                rs.getInt("branch_id"),
                rs.getString("branch"),
                rs.getObject("client_id", Long.class),
                rs.getString("client"),
                rs.getLong("cashier_id"),
                rs.getString("cashier"),
                rs.getObject("promo_id", Integer.class),
                rs.getString("promo_code"),
                rs.getString("channel"),
                rs.getString("status"),
                rs.getBigDecimal("total"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getObject("modified_by", Long.class),
                rs.getLong("lines_count"));
    }

    private static Rows.OrderLine line(ResultSet rs) throws SQLException {
        return new Rows.OrderLine(
                rs.getLong("id"),
                rs.getLong("order_id"),
                rs.getString("order_status"),
                rs.getLong("menu_item_id"),
                rs.getString("menu_item"),
                rs.getString("section"),
                rs.getInt("quantity"),
                rs.getBigDecimal("price_at_sale"),
                rs.getBigDecimal("line_total"));
    }

    private static Rows.Payment payment(ResultSet rs) throws SQLException {
        return new Rows.Payment(
                rs.getLong("id"),
                rs.getLong("order_id"),
                rs.getString("order_status"),
                rs.getString("method"),
                rs.getBigDecimal("amount"),
                rs.getObject("paid_at", LocalDateTime.class));
    }
}
