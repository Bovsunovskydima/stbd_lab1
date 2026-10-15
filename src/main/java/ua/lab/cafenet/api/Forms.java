package ua.lab.cafenet.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class Forms {

    private Forms() {
    }

    public record Created(long id) {
    }

    public record BranchForm(@NotBlank @Size(max = 100) String name,
                             @NotBlank @Size(max = 80) String city,
                             @NotBlank @Size(max = 200) String address,
                             LocalTime opensAt,
                             LocalTime closesAt) {
    }

    public record PositionForm(@NotBlank @Size(max = 60) String title,
                               @DecimalMin("0.0") BigDecimal baseRate) {
    }

    public record StaffForm(@Positive int branchId,
                            @Positive int positionId,
                            @NotBlank @Size(max = 150) String fullName,
                            @Size(max = 20) String phone,
                            @NotNull LocalDate hiredOn) {
    }

    public record StaffEditForm(@Positive int positionId,
                                @Size(max = 20) String phone) {
    }

    public record BranchRefForm(@Positive int branchId) {
    }

    public record ShiftForm(@Positive long staffId,
                            @NotNull LocalDateTime startsAt,
                            @NotNull LocalDateTime endsAt) {
    }

    public record ClientForm(@NotBlank @Size(max = 150) String fullName,
                             @NotBlank @Size(max = 20) String phone,
                             @Size(max = 120) String email) {
    }

    public record AddressForm(@NotBlank @Size(max = 150) String street,
                              @NotBlank @Size(max = 20) String building,
                              @Size(max = 10) String apartment,
                              boolean asDefault) {
    }

    public record PointsForm(int points) {
    }

    public record PromoForm(@NotBlank @Size(max = 30) String code,
                            @NotNull @DecimalMin("0.01") BigDecimal discountPercent,
                            @NotNull LocalDate validTo) {
    }

    public record PromoEditForm(@NotNull @DecimalMin("0.01") BigDecimal discountPercent,
                                @NotNull LocalDate validTo) {
    }

    public record SectionForm(@NotBlank @Size(max = 80) String name,
                              Integer sortOrder) {
    }

    public record MenuItemForm(@Positive int sectionId,
                               @NotBlank @Size(max = 120) String title,
                               @Positive Integer weightG,
                               @NotNull @DecimalMin("0.01") BigDecimal price) {
    }

    public record PriceForm(@NotNull @DecimalMin("0.01") BigDecimal price) {
    }

    public record TechCardForm(@Positive long productId,
                               @NotNull @DecimalMin("0.001") BigDecimal amount) {
    }

    public record ProductForm(@NotBlank @Size(max = 100) String name,
                              @NotBlank @Pattern(regexp = "kg|l|pcs") String unit) {
    }

    public record SupplierForm(@NotBlank @Size(max = 120) String name,
                               @Size(max = 120) String contact) {
    }

    public record PurchaseForm(@Positive int branchId,
                               @Positive int supplierId) {
    }

    public record PurchaseLineForm(@Positive long productId,
                                   @NotNull @DecimalMin("0.001") BigDecimal quantity,
                                   @NotNull @DecimalMin("0.00") BigDecimal unitCost) {
    }

    public record StockForm(@Positive int branchId,
                            @Positive long productId,
                            @NotNull BigDecimal delta,
                            @NotBlank @Pattern(regexp = "PURCHASE|SALE|WRITE_OFF|CORRECTION") String reason) {
    }

    public record OrderForm(@Positive int branchId,
                            Long clientId,
                            @Positive long cashierId,
                            @NotBlank @Pattern(regexp = "HALL|TAKEAWAY|DELIVERY") String channel) {
    }

    public record OrderLineForm(@Positive long menuItemId,
                                @Positive int quantity) {
    }

    public record PromoCodeForm(@NotBlank @Size(max = 30) String code) {
    }

    public record PaymentForm(@NotBlank @Pattern(regexp = "CASH|CARD|ONLINE") String method) {
    }

    public record DeliveryForm(@Positive long addressId) {
    }

    public record CourierForm(@Positive long staffId,
                              @Size(max = 30) String vehicle) {
    }

    public record AssignForm(@Positive int courierId) {
    }

    public record ReviewForm(@Positive long orderId,
                             @Min(1) @Max(5) short rating,
                             String comment) {
    }

    public record CheckoutForm(@Positive int branchId,
                               @Positive long cashierId,
                               Long clientId,
                               @Size(max = 150) String clientName,
                               @Size(max = 20) String clientPhone,
                               @NotBlank @Pattern(regexp = "HALL|TAKEAWAY|DELIVERY") String channel,
                               @NotEmpty @Valid List<OrderLineForm> items,
                               @Size(max = 30) String promoCode,
                               @NotBlank @Pattern(regexp = "CASH|CARD|ONLINE") String paymentMethod,
                               Long addressId,
                               @Size(max = 150) String street,
                               @Size(max = 20) String building,
                               @Size(max = 10) String apartment) {
    }
}
