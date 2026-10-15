package ua.lab.cafenet.service;

import org.springframework.stereotype.Service;
import ua.lab.cafenet.api.Forms;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.NotFoundException;
import ua.lab.cafenet.persistence.UnitOfWorkFactory;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ClientService {

    private final UnitOfWorkFactory factory;

    public ClientService(UnitOfWorkFactory factory) {
        this.factory = factory;
    }

    public List<Rows.Client> list(String name) {
        return factory.readOnly(work -> work.clients().search(name));
    }

    public Rows.Client byId(long id) {
        return factory.readOnly(work -> work.clients().byId(id))
                .orElseThrow(() -> NotFoundException.of("Клієнта", id));
    }

    public BigDecimal spent(long id) {
        return factory.readOnly(work -> {
            work.clients().byId(id).orElseThrow(() -> NotFoundException.of("Клієнта", id));
            return work.clients().spent(id);
        });
    }

    public long create(Long actor, Forms.ClientForm form) {
        return factory.inTransaction(actor, work -> work.clients()
                .add(form.fullName(), form.phone(), form.email()));
    }

    public void update(Long actor, long id, Forms.ClientForm form) {
        factory.run(actor, work -> work.clients().edit(id, form.fullName(), form.phone(), form.email()));
    }

    public void remove(Long actor, long id) {
        factory.run(actor, work -> work.clients().remove(id));
    }

    public void restore(Long actor, long id) {
        factory.run(actor, work -> work.clients().restore(id));
    }

    public List<Rows.Address> addresses(long clientId) {
        return factory.readOnly(work -> work.clients().addresses(clientId));
    }

    public long addAddress(Long actor, long clientId, Forms.AddressForm form) {
        return factory.inTransaction(actor, work -> work.clients()
                .addAddress(clientId, form.street(), form.building(), form.apartment(), form.asDefault()));
    }

    public void makeDefault(Long actor, long addressId) {
        factory.run(actor, work -> work.clients().makeDefault(addressId));
    }

    public Rows.LoyaltyCard card(long clientId) {
        return factory.readOnly(work -> work.clients().card(clientId))
                .orElseThrow(() -> new NotFoundException("Картка лояльності клієнта " + clientId + " не знайдена"));
    }

    public long issueCard(Long actor, long clientId) {
        return factory.inTransaction(actor, work -> work.clients().issueCard(clientId));
    }

    public void adjustPoints(Long actor, long clientId, int points) {
        factory.run(actor, work -> work.clients().adjustPoints(clientId, points));
    }
}
