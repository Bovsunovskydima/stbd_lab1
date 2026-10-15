package ua.lab.cafenet.service;

import org.springframework.stereotype.Service;
import ua.lab.cafenet.api.Forms;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.NotFoundException;
import ua.lab.cafenet.persistence.UnitOfWorkFactory;

import java.util.List;

@Service
public class MenuService {

    private final UnitOfWorkFactory factory;

    public MenuService(UnitOfWorkFactory factory) {
        this.factory = factory;
    }

    public List<Rows.Section> sections() {
        return factory.readOnly(work -> work.menu().sections());
    }

    public int createSection(Long actor, Forms.SectionForm form) {
        return factory.inTransaction(actor, work -> work.menu().addSection(form.name(), form.sortOrder()));
    }

    public List<Rows.MenuItem> items(Integer sectionId, String title) {
        return factory.readOnly(work -> work.menu().items(sectionId, title));
    }

    public Rows.MenuItem byId(long id) {
        return factory.readOnly(work -> work.menu().byId(id))
                .orElseThrow(() -> NotFoundException.of("Позицію меню", id));
    }

    public long create(Long actor, Forms.MenuItemForm form) {
        return factory.inTransaction(actor, work -> work.menu()
                .add(form.sectionId(), form.title(), form.weightG(), form.price()));
    }

    public void update(Long actor, long id, Forms.MenuItemForm form) {
        factory.run(actor, work -> work.menu()
                .edit(id, form.sectionId(), form.title(), form.weightG(), form.price()));
    }

    public void reprice(Long actor, long id, Forms.PriceForm form) {
        factory.run(actor, work -> work.menu().reprice(id, form.price()));
    }

    public void remove(Long actor, long id) {
        factory.run(actor, work -> work.menu().remove(id));
    }

    public void restore(Long actor, long id) {
        factory.run(actor, work -> work.menu().restore(id));
    }

    public List<Rows.TechCard> techCards(long menuItemId) {
        return factory.readOnly(work -> work.menu().techCards(menuItemId));
    }

    public void setTechCard(Long actor, long menuItemId, Forms.TechCardForm form) {
        factory.run(actor, work -> work.menu().setTechCard(menuItemId, form.productId(), form.amount()));
    }
}
