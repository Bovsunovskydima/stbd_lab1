package ua.lab.cafenet.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class Rows {

    private Rows() {
    }

    public record Branch(int id, String name, String city, String address,
                         LocalTime opensAt, LocalTime closesAt, long staffCount) {
    }

    public record Position(int id, String title, BigDecimal baseRate, long staffCount) {
    }

    public record Staff(long id, int branchId, String branch, int positionId, String position,
                        String fullName, String phone, LocalDate hiredOn,
                        LocalDateTime modifiedAt, Long modifiedBy, String modifiedByName) {
    }

    public record Shift(long id, long staffId, String staffName, String branch,
                        LocalDateTime startsAt, LocalDateTime endsAt, BigDecimal hours) {
    }

    public record Client(long id, String fullName, String phone, String email,
                         LocalDateTime modifiedAt, Long modifiedBy, long ordersCount) {
    }

    public record Address(long id, long clientId, String client, String street, String building,
                          String apartment, boolean defaultAddress, String fullAddress) {
    }

    public record LoyaltyCard(long cardId, long clientId, String client, int points,
                              String tier, LocalDate issuedOn) {
    }

    public record Promo(int id, String code, BigDecimal discountPercent, LocalDate validTo,
                        boolean valid, LocalDateTime modifiedAt, Long modifiedBy) {
    }

    public record Section(int id, String name, int sortOrder, long itemsCount) {
    }

    public record MenuItem(long id, int sectionId, String section, String title, Integer weightG,
                           BigDecimal price, LocalDateTime modifiedAt, Long modifiedBy) {
    }

    public record TechCard(long menuItemId, String menuItem, long productId, String product,
                           String unit, BigDecimal amount) {
    }

    public record Product(long id, String name, String unit, LocalDateTime modifiedAt, Long modifiedBy) {
    }

    public record Supplier(int id, String name, String contact, long purchasesCount,
                           LocalDateTime lastPurchaseAt) {
    }

    public record Purchase(long id, int branchId, String branch, int supplierId, String supplier,
                           String status, LocalDateTime createdAt, long linesCount, BigDecimal totalCost) {
    }

    public record PurchaseLine(long id, long purchaseId, String purchaseStatus, long productId,
                               String product, String unit, BigDecimal quantity,
                               BigDecimal unitCost, BigDecimal lineCost) {
    }

    public record StockBalance(int branchId, String branch, long productId, String product,
                               String unit, BigDecimal balance) {
    }

    public record Order(long id, int branchId, String branch, Long clientId, String client,
                        long cashierId, String cashier, Integer promoId, String promoCode,
                        String channel, String status, BigDecimal total, LocalDateTime createdAt,
                        LocalDateTime modifiedAt, Long modifiedBy, long linesCount) {
    }

    public record OrderLine(long id, long orderId, String orderStatus, long menuItemId, String menuItem,
                            String section, int quantity, BigDecimal priceAtSale, BigDecimal lineTotal) {
    }

    public record Payment(long id, long orderId, String orderStatus, String method,
                          BigDecimal amount, LocalDateTime paidAt) {
    }

    public record Delivery(long id, long orderId, String orderStatus, BigDecimal total, Integer courierId,
                           String courier, String vehicle, String client, String address,
                           LocalDateTime deliveredAt) {
    }

    public record Review(long id, long orderId, String branch, String client, short rating, String comment) {
    }

    public record FreeCourier(int courierId, long staffId, String fullName, String vehicle) {
    }

    public record CourierStat(int courierId, String courier, String vehicle, long assigned, long delivered) {
    }

    public record RevenueDay(LocalDate day, int branchId, String branch, long ordersCount,
                             BigDecimal revenue, BigDecimal avgCheck) {
    }

    public record MenuTop(long menuItemId, String menuItem, String section, long soldQty, BigDecimal revenue) {
    }

    public record OrderCard(Order order, List<OrderLine> lines, List<Payment> payments) {
    }

    public record CheckoutResult(Long clientId, long orderId, long paymentId, Long deliveryId,
                                 BigDecimal total, List<OrderLine> lines) {
    }
}
