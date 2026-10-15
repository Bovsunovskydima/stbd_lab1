package ua.lab.cafenet.service;

import org.springframework.stereotype.Service;
import ua.lab.cafenet.api.Forms;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.UnitOfWorkFactory;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StockService {

    private final UnitOfWorkFactory factory;

    public StockService(UnitOfWorkFactory factory) {
        this.factory = factory;
    }

    public List<Rows.StockBalance> balances(Integer branchId) {
        return factory.readOnly(work -> work.stock().balances(branchId));
    }

    public List<Rows.StockBalance> low() {
        return factory.readOnly(work -> work.stock().low());
    }

    public BigDecimal balance(int branchId, long productId) {
        return factory.readOnly(work -> work.stock().balanceOf(branchId, productId));
    }

    public long correct(Long actor, Forms.StockForm form) {
        return factory.inTransaction(actor, work -> work.stock()
                .correct(form.branchId(), form.productId(), form.delta(), form.reason()));
    }
}
