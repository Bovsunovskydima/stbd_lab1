package ua.lab.cafenet.service;

import org.springframework.stereotype.Service;
import ua.lab.cafenet.api.Forms;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.NotFoundException;
import ua.lab.cafenet.persistence.UnitOfWorkFactory;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SalesService {

    private final UnitOfWorkFactory factory;

    public SalesService(UnitOfWorkFactory factory) {
        this.factory = factory;
    }

    public List<Rows.Order> list(Integer branchId, String status, String channel) {
        return factory.readOnly(work -> work.sales().search(branchId, status, channel));
    }

    public Rows.OrderCard card(long id) {
        return factory.readOnly(work -> {
            Rows.Order order = work.sales().byId(id)
                    .orElseThrow(() -> NotFoundException.of("Замовлення", id));
            return new Rows.OrderCard(order, work.sales().lines(id), work.sales().payments(id));
        });
    }

    public List<Rows.OrderLine> lines(long id) {
        return factory.readOnly(work -> work.sales().lines(id));
    }

    public BigDecimal amount(long id) {
        return factory.readOnly(work -> work.sales().amount(id));
    }

    public long open(Long actor, Forms.OrderForm form) {
        return factory.inTransaction(actor, work -> work.sales()
                .open(form.branchId(), form.clientId(), form.cashierId(), form.channel()));
    }

    public long addLine(Long actor, long orderId, Forms.OrderLineForm form) {
        return factory.inTransaction(actor, work -> work.sales()
                .addLine(orderId, form.menuItemId(), form.quantity()));
    }

    public void dropLine(Long actor, long lineId) {
        factory.run(actor, work -> work.sales().dropLine(lineId));
    }

    public void applyPromo(Long actor, long orderId, Forms.PromoCodeForm form) {
        factory.run(actor, work -> work.sales().applyPromo(orderId, form.code()));
    }

    public long pay(Long actor, long orderId, Forms.PaymentForm form) {
        return factory.inTransaction(actor, work -> work.sales().pay(orderId, form.method()));
    }

    public void close(Long actor, long orderId) {
        factory.run(actor, work -> work.sales().close(orderId));
    }

    public void cancel(Long actor, long orderId) {
        factory.run(actor, work -> work.sales().cancel(orderId));
    }
}
