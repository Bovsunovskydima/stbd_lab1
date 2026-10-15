package ua.lab.cafenet.service;

import org.springframework.stereotype.Service;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.UnitOfWorkFactory;

import java.util.List;

@Service
public class ReportService {

    private final UnitOfWorkFactory factory;

    public ReportService(UnitOfWorkFactory factory) {
        this.factory = factory;
    }

    public List<Rows.RevenueDay> revenue(Integer branchId) {
        return factory.readOnly(work -> work.reports().revenue(branchId));
    }

    public List<Rows.MenuTop> topMenu() {
        return factory.readOnly(work -> work.reports().topMenu());
    }

    public List<Rows.CourierStat> couriers() {
        return factory.readOnly(work -> work.reports().couriers());
    }

    public List<Rows.StockBalance> lowStock() {
        return factory.readOnly(work -> work.reports().lowStock());
    }
}
