package ua.lab.cafenet.persistence.repository;

import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.StatementRunner;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class StockRepository {

    private static final String COLUMNS = "branch_id, branch, product_id, product, unit, balance";

    private final StatementRunner runner;

    public StockRepository(StatementRunner runner) {
        this.runner = runner;
    }

    public List<Rows.StockBalance> balances(Integer branchId) {
        if (branchId == null) {
            return runner.list("SELECT " + COLUMNS + " FROM v_stock ORDER BY branch, product",
                    StockRepository::balance);
        }
        return runner.list("SELECT " + COLUMNS + " FROM v_stock WHERE branch_id = ? ORDER BY product",
                StockRepository::balance, branchId);
    }

    public List<Rows.StockBalance> low() {
        return runner.list("SELECT " + COLUMNS + " FROM v_stock_low ORDER BY balance, branch",
                StockRepository::balance);
    }

    public BigDecimal balanceOf(int branchId, long productId) {
        return runner.scalar("SELECT fn_stock_balance(CAST(? AS INT), CAST(? AS BIGINT))",
                rs -> rs.getBigDecimal(1), branchId, productId);
    }

    public long correct(int branchId, long productId, BigDecimal delta, String reason) {
        return runner.execId("""
                CALL sp_stock_correct(CAST(? AS INT), CAST(? AS BIGINT), CAST(? AS NUMERIC),
                                      CAST(? AS TEXT), NULL)""",
                branchId, productId, delta, reason);
    }

    private static Rows.StockBalance balance(ResultSet rs) throws SQLException {
        return new Rows.StockBalance(
                rs.getInt("branch_id"),
                rs.getString("branch"),
                rs.getLong("product_id"),
                rs.getString("product"),
                rs.getString("unit"),
                rs.getBigDecimal("balance"));
    }
}
