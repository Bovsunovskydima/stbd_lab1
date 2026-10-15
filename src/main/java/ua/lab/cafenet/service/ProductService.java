package ua.lab.cafenet.service;

import org.springframework.stereotype.Service;
import ua.lab.cafenet.api.Forms;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.NotFoundException;
import ua.lab.cafenet.persistence.UnitOfWorkFactory;

import java.util.List;

@Service
public class ProductService {

    private final UnitOfWorkFactory factory;

    public ProductService(UnitOfWorkFactory factory) {
        this.factory = factory;
    }

    public List<Rows.Product> list(String name) {
        return factory.readOnly(work -> work.products().search(name));
    }

    public Rows.Product byId(long id) {
        return factory.readOnly(work -> work.products().byId(id))
                .orElseThrow(() -> NotFoundException.of("Продукт", id));
    }

    public long create(Long actor, Forms.ProductForm form) {
        return factory.inTransaction(actor, work -> work.products().add(form.name(), form.unit()));
    }

    public void update(Long actor, long id, Forms.ProductForm form) {
        factory.run(actor, work -> work.products().edit(id, form.name(), form.unit()));
    }

    public void remove(Long actor, long id) {
        factory.run(actor, work -> work.products().remove(id));
    }

    public void restore(Long actor, long id) {
        factory.run(actor, work -> work.products().restore(id));
    }
}
