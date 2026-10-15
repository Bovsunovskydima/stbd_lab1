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

public class ClientRepository {

    private static final String COLUMNS = "id, full_name, phone, email, modified_at, modified_by, orders_count";
    private static final String ADDRESS_COLUMNS =
            "id, client_id, client, street, building, apartment, is_default, full_address";
    private static final String CARD_COLUMNS = "card_id, client_id, client, points, tier, issued_on";

    private final StatementRunner runner;

    public ClientRepository(StatementRunner runner) {
        this.runner = runner;
    }

    public List<Rows.Client> search(String name) {
        if (name == null || name.isBlank()) {
            return runner.list("SELECT " + COLUMNS + " FROM v_clients ORDER BY full_name",
                    ClientRepository::client);
        }
        return runner.list("""
                SELECT %s FROM v_clients
                WHERE full_name ILIKE '%%' || CAST(? AS TEXT) || '%%'
                ORDER BY full_name""".formatted(COLUMNS), ClientRepository::client, name.trim());
    }

    public Optional<Rows.Client> byId(long id) {
        return runner.single("SELECT " + COLUMNS + " FROM v_clients WHERE id = ?", ClientRepository::client, id);
    }

    public long add(String fullName, String phone, String email) {
        return runner.execId("CALL sp_client_add(CAST(? AS TEXT), CAST(? AS TEXT), CAST(? AS TEXT), NULL)",
                fullName, phone, email);
    }

    public void edit(long id, String fullName, String phone, String email) {
        runner.exec("CALL sp_client_edit(CAST(? AS BIGINT), CAST(? AS TEXT), CAST(? AS TEXT), CAST(? AS TEXT))",
                id, fullName, phone, email);
    }

    public void remove(long id) {
        runner.exec("CALL sp_client_drop(CAST(? AS BIGINT))", id);
    }

    public void restore(long id) {
        runner.exec("CALL sp_client_restore(CAST(? AS BIGINT))", id);
    }

    public BigDecimal spent(long id) {
        return runner.scalar("SELECT fn_client_spent(CAST(? AS BIGINT))", rs -> rs.getBigDecimal(1), id);
    }

    public List<Rows.Address> addresses(long clientId) {
        return runner.list("""
                SELECT %s FROM v_client_addresses
                WHERE client_id = ?
                ORDER BY is_default DESC, street""".formatted(ADDRESS_COLUMNS),
                ClientRepository::address, clientId);
    }

    public Optional<Rows.Address> defaultAddress(long clientId) {
        return runner.single("""
                SELECT %s FROM v_client_addresses
                WHERE client_id = ? AND is_default""".formatted(ADDRESS_COLUMNS),
                ClientRepository::address, clientId);
    }

    public long addAddress(long clientId, String street, String building, String apartment, boolean asDefault) {
        return runner.execId("""
                CALL sp_address_add(CAST(? AS BIGINT), CAST(? AS TEXT), CAST(? AS TEXT),
                                    CAST(? AS TEXT), CAST(? AS BOOLEAN), NULL)""",
                clientId, street, building, apartment, asDefault);
    }

    public void makeDefault(long addressId) {
        runner.exec("CALL sp_address_default(CAST(? AS BIGINT))", addressId);
    }

    public Optional<Rows.LoyaltyCard> card(long clientId) {
        return runner.single("SELECT " + CARD_COLUMNS + " FROM v_loyalty WHERE client_id = ?",
                ClientRepository::card, clientId);
    }

    public long issueCard(long clientId) {
        return runner.execId("CALL sp_loyalty_issue(CAST(? AS BIGINT), NULL)", clientId);
    }

    public void adjustPoints(long clientId, int points) {
        runner.exec("CALL sp_loyalty_adjust(CAST(? AS BIGINT), CAST(? AS INT))", clientId, points);
    }

    private static Rows.Client client(ResultSet rs) throws SQLException {
        return new Rows.Client(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getObject("modified_by", Long.class),
                rs.getLong("orders_count"));
    }

    private static Rows.Address address(ResultSet rs) throws SQLException {
        return new Rows.Address(
                rs.getLong("id"),
                rs.getLong("client_id"),
                rs.getString("client"),
                rs.getString("street"),
                rs.getString("building"),
                rs.getString("apartment"),
                rs.getBoolean("is_default"),
                rs.getString("full_address"));
    }

    private static Rows.LoyaltyCard card(ResultSet rs) throws SQLException {
        return new Rows.LoyaltyCard(
                rs.getLong("card_id"),
                rs.getLong("client_id"),
                rs.getString("client"),
                rs.getInt("points"),
                rs.getString("tier"),
                rs.getObject("issued_on", LocalDate.class));
    }
}
