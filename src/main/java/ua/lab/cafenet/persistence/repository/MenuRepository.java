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

public class MenuRepository {

    private static final String ITEM_COLUMNS =
            "id, section_id, section, title, weight_g, price, modified_at, modified_by";
    private static final String SECTION_COLUMNS = "id, name, sort_order, items_count";
    private static final String CARD_COLUMNS = "menu_item_id, menu_item, product_id, product, unit, amount";

    private final StatementRunner runner;

    public MenuRepository(StatementRunner runner) {
        this.runner = runner;
    }

    public List<Rows.Section> sections() {
        return runner.list("SELECT " + SECTION_COLUMNS + " FROM v_menu_sections ORDER BY sort_order, name",
                MenuRepository::section);
    }

    public int addSection(String name, Integer sortOrder) {
        return (int) runner.execId("CALL sp_section_add(CAST(? AS TEXT), CAST(? AS INT), NULL)", name, sortOrder);
    }

    public List<Rows.MenuItem> items(Integer sectionId, String title) {
        StringBuilder sql = new StringBuilder("SELECT " + ITEM_COLUMNS + " FROM v_menu WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (sectionId != null) {
            sql.append(" AND section_id = ?");
            args.add(sectionId);
        }
        if (title != null && !title.isBlank()) {
            sql.append(" AND title ILIKE '%' || CAST(? AS TEXT) || '%'");
            args.add(title.trim());
        }
        sql.append(" ORDER BY section, title");
        return runner.list(sql.toString(), MenuRepository::item, args.toArray());
    }

    public Optional<Rows.MenuItem> byId(long id) {
        return runner.single("SELECT " + ITEM_COLUMNS + " FROM v_menu WHERE id = ?", MenuRepository::item, id);
    }

    public long add(int sectionId, String title, Integer weight, BigDecimal price) {
        return runner.execId("""
                CALL sp_menu_add(CAST(? AS INT), CAST(? AS TEXT), CAST(? AS INT), CAST(? AS NUMERIC), NULL)""",
                sectionId, title, weight, price);
    }

    public void edit(long id, int sectionId, String title, Integer weight, BigDecimal price) {
        runner.exec("""
                CALL sp_menu_edit(CAST(? AS BIGINT), CAST(? AS INT), CAST(? AS TEXT),
                                  CAST(? AS INT), CAST(? AS NUMERIC))""",
                id, sectionId, title, weight, price);
    }

    public void reprice(long id, BigDecimal price) {
        runner.exec("CALL sp_menu_reprice(CAST(? AS BIGINT), CAST(? AS NUMERIC))", id, price);
    }

    public void remove(long id) {
        runner.exec("CALL sp_menu_drop(CAST(? AS BIGINT))", id);
    }

    public void restore(long id) {
        runner.exec("CALL sp_menu_restore(CAST(? AS BIGINT))", id);
    }

    public List<Rows.TechCard> techCards(long menuItemId) {
        return runner.list("SELECT " + CARD_COLUMNS + " FROM v_tech_cards WHERE menu_item_id = ? ORDER BY product",
                MenuRepository::techCard, menuItemId);
    }

    public void setTechCard(long menuItemId, long productId, BigDecimal amount) {
        runner.exec("CALL sp_tech_card_set(CAST(? AS BIGINT), CAST(? AS BIGINT), CAST(? AS NUMERIC))",
                menuItemId, productId, amount);
    }

    private static Rows.Section section(ResultSet rs) throws SQLException {
        return new Rows.Section(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("sort_order"),
                rs.getLong("items_count"));
    }

    private static Rows.MenuItem item(ResultSet rs) throws SQLException {
        return new Rows.MenuItem(
                rs.getLong("id"),
                rs.getInt("section_id"),
                rs.getString("section"),
                rs.getString("title"),
                rs.getObject("weight_g", Integer.class),
                rs.getBigDecimal("price"),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getObject("modified_by", Long.class));
    }

    private static Rows.TechCard techCard(ResultSet rs) throws SQLException {
        return new Rows.TechCard(
                rs.getLong("menu_item_id"),
                rs.getString("menu_item"),
                rs.getLong("product_id"),
                rs.getString("product"),
                rs.getString("unit"),
                rs.getBigDecimal("amount"));
    }
}
