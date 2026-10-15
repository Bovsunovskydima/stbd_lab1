package ua.lab.cafenet.service;

import org.springframework.stereotype.Service;
import ua.lab.cafenet.api.Forms;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.NotFoundException;
import ua.lab.cafenet.persistence.UnitOfWorkFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeliveryService {

    private final UnitOfWorkFactory factory;

    public DeliveryService(UnitOfWorkFactory factory) {
        this.factory = factory;
    }

    public List<Rows.Delivery> list(Boolean pending) {
        return factory.readOnly(work -> work.deliveries().all(pending));
    }

    public Rows.Delivery byId(long id) {
        return factory.readOnly(work -> work.deliveries().byId(id))
                .orElseThrow(() -> NotFoundException.of("Доставку", id));
    }

    public long create(Long actor, long orderId, Forms.DeliveryForm form) {
        return factory.inTransaction(actor, work -> work.deliveries().create(orderId, form.addressId()));
    }

    public void assign(Long actor, long deliveryId, Forms.AssignForm form) {
        factory.run(actor, work -> work.deliveries().assign(deliveryId, form.courierId()));
    }

    public void complete(Long actor, long deliveryId) {
        factory.run(actor, work -> work.deliveries().complete(deliveryId));
    }

    public int addCourier(Long actor, Forms.CourierForm form) {
        return factory.inTransaction(actor, work -> work.deliveries()
                .addCourier(form.staffId(), form.vehicle()));
    }

    public List<Rows.CourierStat> couriers() {
        return factory.readOnly(work -> work.deliveries().couriers());
    }

    public List<Rows.FreeCourier> free(LocalDateTime from, LocalDateTime to) {
        return factory.readOnly(work -> work.deliveries().freeCouriers(from, to));
    }
}
