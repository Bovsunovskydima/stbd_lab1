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

import java.sql.Connection;
import java.sql.SQLException;

final class JdbcUnitOfWork implements UnitOfWork {

    private final Connection connection;
    private final StatementRunner runner;

    private BranchRepository branches;
    private StaffRepository staff;
    private ClientRepository clients;
    private PromoRepository promos;
    private MenuRepository menu;
    private ProductRepository products;
    private SupplyRepository supply;
    private StockRepository stock;
    private SalesRepository sales;
    private DeliveryRepository deliveries;
    private ReviewRepository reviews;
    private ReportRepository reports;

    private boolean finished;

    JdbcUnitOfWork(Connection connection, Long actor) {
        this.connection = connection;
        this.runner = new StatementRunner(connection);
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            shut();
            throw new StorageException("Не вдалося відкрити одиницю роботи", e);
        }
        runner.exec("CALL sp_actor_set(CAST(? AS BIGINT))", actor);
    }

    @Override
    public BranchRepository branches() {
        if (branches == null) {
            branches = new BranchRepository(runner);
        }
        return branches;
    }

    @Override
    public StaffRepository staff() {
        if (staff == null) {
            staff = new StaffRepository(runner);
        }
        return staff;
    }

    @Override
    public ClientRepository clients() {
        if (clients == null) {
            clients = new ClientRepository(runner);
        }
        return clients;
    }

    @Override
    public PromoRepository promos() {
        if (promos == null) {
            promos = new PromoRepository(runner);
        }
        return promos;
    }

    @Override
    public MenuRepository menu() {
        if (menu == null) {
            menu = new MenuRepository(runner);
        }
        return menu;
    }

    @Override
    public ProductRepository products() {
        if (products == null) {
            products = new ProductRepository(runner);
        }
        return products;
    }

    @Override
    public SupplyRepository supply() {
        if (supply == null) {
            supply = new SupplyRepository(runner);
        }
        return supply;
    }

    @Override
    public StockRepository stock() {
        if (stock == null) {
            stock = new StockRepository(runner);
        }
        return stock;
    }

    @Override
    public SalesRepository sales() {
        if (sales == null) {
            sales = new SalesRepository(runner);
        }
        return sales;
    }

    @Override
    public DeliveryRepository deliveries() {
        if (deliveries == null) {
            deliveries = new DeliveryRepository(runner);
        }
        return deliveries;
    }

    @Override
    public ReviewRepository reviews() {
        if (reviews == null) {
            reviews = new ReviewRepository(runner);
        }
        return reviews;
    }

    @Override
    public ReportRepository reports() {
        if (reports == null) {
            reports = new ReportRepository(runner);
        }
        return reports;
    }

    @Override
    public void commit() {
        try {
            connection.commit();
            finished = true;
        } catch (SQLException e) {
            throw new StorageException("Не вдалося підтвердити транзакцію", e);
        }
    }

    @Override
    public void rollback() {
        try {
            connection.rollback();
            finished = true;
        } catch (SQLException e) {
            throw new StorageException("Не вдалося відкотити транзакцію", e);
        }
    }

    @Override
    public void close() {
        if (!finished) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }
        }
        shut();
    }

    private void shut() {
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
