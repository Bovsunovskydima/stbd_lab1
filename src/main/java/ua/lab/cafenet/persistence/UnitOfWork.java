package ua.lab.cafenet.persistence;

import ua.lab.cafenet.persistence.repository.BranchRepository;
import ua.lab.cafenet.persistence.repository.ClientRepository;
import ua.lab.cafenet.persistence.repository.DeliveryRepository;
import ua.lab.cafenet.persistence.repository.MenuRepository;
import ua.lab.cafenet.persistence.repository.ProductRepository;
import ua.lab.cafenet.persistence.repository.PromoRepository;
import ua.lab.cafenet.persistence.repository.ReportRepository;
import ua.lab.cafenet.persistence.repository.ReviewRepository;
import ua.lab.cafenet.persistence.repository.SalesRepository;
import ua.lab.cafenet.persistence.repository.StaffRepository;
import ua.lab.cafenet.persistence.repository.StockRepository;
import ua.lab.cafenet.persistence.repository.SupplyRepository;

public interface UnitOfWork extends AutoCloseable {

    BranchRepository branches();

    StaffRepository staff();

    ClientRepository clients();

    PromoRepository promos();

    MenuRepository menu();

    ProductRepository products();

    SupplyRepository supply();

    StockRepository stock();

    SalesRepository sales();

    DeliveryRepository deliveries();

    ReviewRepository reviews();

    ReportRepository reports();

    void commit();

    void rollback();

    @Override
    void close();
}
