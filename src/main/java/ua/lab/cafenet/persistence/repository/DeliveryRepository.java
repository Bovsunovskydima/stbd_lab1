package ua.lab.cafenet.persistence.repository;

import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.StatementRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class DeliveryRepository {

    private static final String COLUMNS = """
            id, order_id, order_status, total, courier_id, courier, vehicle, client, address, delivered_at""";

    private static final String STAT_COLUMNS = "courier_id, courier, vehicle, assigned, delivered";

    private final StatementRunner runner;

    public DeliveryRepository(StatementRunner runner) {
        this.runner = runner;
    }

    public List<Rows.Delivery> all(Boolean pending) {
        if (Boolean.TRUE.equals(pending)) {
            return runner.list("SELECT " + COLUMNS + " FROM v_deliveries WHERE delivered_at IS NULL ORDER BY id",
                    DeliveryRepository::delivery);
        }
        return runner.list("SELECT " + COLUMNS + " FROM v_deliveries ORDER BY id DESC",
                DeliveryRepository::delivery);
    }

    public Optional<Rows.Delivery> byId(long id) {
        return runner.single("SELECT " + COLUMNS + " FROM v_deliveries WHERE id = ?",
                DeliveryRepository::delivery, id);
    }

    public Optional<Rows.Delivery> byOrder(long orderId) {
        return runner.single("SELECT " + COLUMNS + " FROM v_deliveries WHERE order_id = ?",
                DeliveryRepository::delivery, orderId);
    }

    public long create(long orderId, long addressId) {
        return runner.execId("CALL sp_delivery_create(CAST(? AS BIGINT), CAST(? AS BIGINT), NULL)",
                orderId, addressId);
    }

    public void assign(long deliveryId, int courierId) {
        runner.exec("CALL sp_delivery_assign(CAST(? AS BIGINT), CAST(? AS INT))", deliveryId, courierId);
    }

    public void complete(long deliveryId) {
        runner.exec("CALL sp_delivery_done(CAST(? AS BIGINT))", deliveryId);
    }

    public int addCourier(long staffId, String vehicle) {
        return (int) runner.execId("CALL sp_courier_add(CAST(? AS BIGINT), CAST(? AS TEXT), NULL)",
                staffId, vehicle);
    }

    public List<Rows.CourierStat> couriers() {
        return runner.list("SELECT " + STAT_COLUMNS + " FROM v_courier_stats ORDER BY courier",
                DeliveryRepository::stat);
    }

    public List<Rows.FreeCourier> freeCouriers(LocalDateTime from, LocalDateTime to) {
        return runner.list("""
                SELECT courier_id, staff_id, full_name, vehicle
                FROM fn_free_couriers(CAST(? AS TIMESTAMP), CAST(? AS TIMESTAMP))""",
                DeliveryRepository::free, from, to);
    }

    private static Rows.Delivery delivery(ResultSet rs) throws SQLException {
        return new Rows.Delivery(
                rs.getLong("id"),
                rs.getLong("order_id"),
                rs.getString("order_status"),
                rs.getBigDecimal("total"),
                rs.getObject("courier_id", Integer.class),
                rs.getString("courier"),
                rs.getString("vehicle"),
                rs.getString("client"),
                rs.getString("address"),
                rs.getObject("delivered_at", LocalDateTime.class));
    }

    private static Rows.CourierStat stat(ResultSet rs) throws SQLException {
        return new Rows.CourierStat(
                rs.getInt("courier_id"),
                rs.getString("courier"),
                rs.getString("vehicle"),
                rs.getLong("assigned"),
                rs.getLong("delivered"));
    }

    private static Rows.FreeCourier free(ResultSet rs) throws SQLException {
        return new Rows.FreeCourier(
                rs.getInt("courier_id"),
                rs.getLong("staff_id"),
                rs.getString("full_name"),
                rs.getString("vehicle"));
    }
}
