package ua.lab.cafenet.service;

import org.springframework.stereotype.Service;
import ua.lab.cafenet.api.Forms;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.NotFoundException;
import ua.lab.cafenet.persistence.RuleViolationException;
import ua.lab.cafenet.persistence.UnitOfWork;
import ua.lab.cafenet.persistence.UnitOfWorkFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CheckoutService {

    private static final String DELIVERY = "DELIVERY";

    private final UnitOfWorkFactory factory;

    public CheckoutService(UnitOfWorkFactory factory) {
        this.factory = factory;
    }

    public Rows.CheckoutResult checkout(Long actor, Forms.CheckoutForm form) {
        try (UnitOfWork work = factory.open(actor)) {
            try {
                Long clientId = resolveClient(work, form);

                long orderId = work.sales()
                        .open(form.branchId(), clientId, form.cashierId(), form.channel());

                for (Forms.OrderLineForm item : form.items()) {
                    work.sales().addLine(orderId, item.menuItemId(), item.quantity());
                }

                if (form.promoCode() != null && !form.promoCode().isBlank()) {
                    work.sales().applyPromo(orderId, form.promoCode());
                }

                long paymentId = work.sales().pay(orderId, form.paymentMethod());
                work.sales().close(orderId);

                Long deliveryId = null;
                if (DELIVERY.equals(form.channel())) {
                    deliveryId = arrangeDelivery(work, orderId, clientId, form);
                }

                Rows.Order order = work.sales().byId(orderId)
                        .orElseThrow(() -> NotFoundException.of("Замовлення", orderId));

                Rows.CheckoutResult result = new Rows.CheckoutResult(
                        clientId, orderId, paymentId, deliveryId, order.total(), work.sales().lines(orderId));

                work.commit();
                return result;
            } catch (RuntimeException e) {
                work.rollback();
                throw e;
            }
        }
    }

    private Long resolveClient(UnitOfWork work, Forms.CheckoutForm form) {
        if (form.clientId() != null) {
            return work.clients().byId(form.clientId())
                    .orElseThrow(() -> NotFoundException.of("Клієнта", form.clientId()))
                    .id();
        }
        boolean hasNewClient = form.clientName() != null && !form.clientName().isBlank()
                && form.clientPhone() != null && !form.clientPhone().isBlank();
        if (hasNewClient) {
            return work.clients().add(form.clientName(), form.clientPhone(), null);
        }
        if (DELIVERY.equals(form.channel())) {
            throw new RuleViolationException("Для доставки потрібно вказати клієнта");
        }
        return null;
    }

    private long arrangeDelivery(UnitOfWork work, long orderId, Long clientId, Forms.CheckoutForm form) {
        long addressId = resolveAddress(work, clientId, form);
        long deliveryId = work.deliveries().create(orderId, addressId);

        LocalDateTime from = LocalDateTime.now();
        List<Rows.FreeCourier> free = work.deliveries().freeCouriers(from, from.plusHours(1));
        if (free.isEmpty()) {
            throw new RuleViolationException("На найближчу годину немає вільного курєра");
        }
        work.deliveries().assign(deliveryId, free.getFirst().courierId());
        return deliveryId;
    }

    private long resolveAddress(UnitOfWork work, Long clientId, Forms.CheckoutForm form) {
        if (form.addressId() != null) {
            return form.addressId();
        }
        boolean hasNewAddress = form.street() != null && !form.street().isBlank()
                && form.building() != null && !form.building().isBlank();
        if (hasNewAddress) {
            return work.clients()
                    .addAddress(clientId, form.street(), form.building(), form.apartment(), true);
        }
        return work.clients().defaultAddress(clientId)
                .orElseThrow(() -> new RuleViolationException("У клієнта немає збереженої адреси доставки"))
                .id();
    }
}
