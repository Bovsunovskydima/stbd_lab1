package ua.lab.cafenet.persistence.repository;

import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.StatementRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReportRepository {

    private final StatementRunner runner;

    public ReportRepository(StatementRunner runner) {
        this.runner = runner;
    }

    public List<Rows.RevenueDay> revenue(Integer branchId) {
        if (branchId == null) {
            return runner.list("""
                    SELECT day, branch_id, branch, orders_count, revenue, avg_check
                    FROM v_revenue_by_day
                    ORDER BY day DESC, branch""", ReportRepository::revenueDay);
        }
        return runner.list("""
                SELECT day, branch_id, branch, orders_count, revenue, avg_check
                FROM v_revenue_by_day
                WHERE branch_id = ?
                ORDER BY day DESC""", ReportRepository::revenueDay, branchId);
    }

    public List<Rows.MenuTop> topMenu() {
        return runner.list("""
                SELECT menu_item_id, menu_item, section, sold_qty, revenue
                FROM v_menu_top
                ORDER BY sold_qty DESC, menu_item""", ReportRepository::menuTop);
    }

    public List<Rows.CourierStat> couriers() {
        return runner.list("""
                SELECT courier_id, courier, vehicle, assigned, delivered
                FROM v_courier_stats
                ORDER BY delivered DESC, courier""", ReportRepository::stat);
    }

    public List<Rows.StockBalance> lowStock() {
        return runner.list("""
                SELECT branch_id, branch, product_id, product, unit, balance
                FROM v_stock_low
                ORDER BY balance, branch""", ReportRepository::stock);
    }

    private static Rows.RevenueDay revenueDay(ResultSet rs) throws SQLException {
        return new Rows.RevenueDay(
                rs.getObject("day", LocalDate.class),
                rs.getInt("branch_id"),
                rs.getString("branch"),
                rs.getLong("orders_count"),
                rs.getBigDecimal("revenue"),
                rs.getBigDecimal("avg_check"));
    }

    private static Rows.MenuTop menuTop(ResultSet rs) throws SQLException {
        return new Rows.MenuTop(
                rs.getLong("menu_item_id"),
                rs.getString("menu_item"),
                rs.getString("section"),
                rs.getLong("sold_qty"),
                rs.getBigDecimal("revenue"));
    }

    private static Rows.CourierStat stat(ResultSet rs) throws SQLException {
        return new Rows.CourierStat(
                rs.getInt("courier_id"),
                rs.getString("courier"),
                rs.getString("vehicle"),
                rs.getLong("assigned"),
                rs.getLong("delivered"));
    }

    private static Rows.StockBalance stock(ResultSet rs) throws SQLException {
        return new Rows.StockBalance(
                rs.getInt("branch_id"),
                rs.getString("branch"),
                rs.getLong("product_id"),
                rs.getString("product"),
                rs.getString("unit"),
                rs.getBigDecimal("balance"));
    }
}
