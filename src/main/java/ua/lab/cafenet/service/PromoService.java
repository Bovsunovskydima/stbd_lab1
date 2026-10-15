package ua.lab.cafenet.service;

import org.springframework.stereotype.Service;
import ua.lab.cafenet.api.Forms;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.NotFoundException;
import ua.lab.cafenet.persistence.UnitOfWorkFactory;

import java.util.List;

@Service
public class PromoService {

    private final UnitOfWorkFactory factory;

    public PromoService(UnitOfWorkFactory factory) {
        this.factory = factory;
    }

    public List<Rows.Promo> list(Boolean onlyValid) {
        return factory.readOnly(work -> work.promos().all(onlyValid));
    }

    public Rows.Promo byId(int id) {
        return factory.readOnly(work -> work.promos().byId(id))
                .orElseThrow(() -> NotFoundException.of("Промокод", id));
    }

    public int create(Long actor, Forms.PromoForm form) {
        return factory.inTransaction(actor, work -> work.promos()
                .add(form.code(), form.discountPercent(), form.validTo()));
    }

    public void update(Long actor, int id, Forms.PromoEditForm form) {
        factory.run(actor, work -> work.promos().edit(id, form.discountPercent(), form.validTo()));
    }

    public void remove(Long actor, int id) {
        factory.run(actor, work -> work.promos().remove(id));
    }

    public void restore(Long actor, int id) {
        factory.run(actor, work -> work.promos().restore(id));
    }
}
