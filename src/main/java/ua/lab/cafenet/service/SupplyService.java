package ua.lab.cafenet.service;

import org.springframework.stereotype.Service;
import ua.lab.cafenet.api.Forms;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.NotFoundException;
import ua.lab.cafenet.persistence.UnitOfWorkFactory;

import java.util.List;

@Service
public class SupplyService {

    private final UnitOfWorkFactory factory;

    public SupplyService(UnitOfWorkFactory factory) {
        this.factory = factory;
    }

    public List<Rows.Supplier> suppliers() {
        return factory.readOnly(work -> work.supply().suppliers());
    }

    public int createSupplier(Long actor, Forms.SupplierForm form) {
        return factory.inTransaction(actor, work -> work.supply().addSupplier(form.name(), form.contact()));
    }

    public List<Rows.Purchase> purchases(Integer branchId, String status) {
        return factory.readOnly(work -> work.supply().purchases(branchId, status));
    }

    public Rows.Purchase purchase(long id) {
        return factory.readOnly(work -> work.supply().purchaseById(id))
                .orElseThrow(() -> NotFoundException.of("Накладну", id));
    }

    public List<Rows.PurchaseLine> lines(long purchaseId) {
        return factory.readOnly(work -> work.supply().lines(purchaseId));
    }

    public long open(Long actor, Forms.PurchaseForm form) {
        return factory.inTransaction(actor, work -> work.supply().open(form.branchId(), form.supplierId()));
    }

    public long addLine(Long actor, long purchaseId, Forms.PurchaseLineForm form) {
        return factory.inTransaction(actor, work -> work.supply()
                .addLine(purchaseId, form.productId(), form.quantity(), form.unitCost()));
    }

    public void receive(Long actor, long purchaseId) {
        factory.run(actor, work -> work.supply().receive(purchaseId));
    }

    public void cancel(Long actor, long purchaseId) {
        factory.run(actor, work -> work.supply().cancel(purchaseId));
    }
}
