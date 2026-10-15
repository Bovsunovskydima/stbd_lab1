package ua.lab.cafenet.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.service.SalesService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class SalesController {

    private final SalesService service;

    public SalesController(SalesService service) {
        this.service = service;
    }

    @GetMapping
    public List<Rows.Order> all(@RequestParam(required = false) Integer branchId,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String channel) {
        return service.list(branchId, status, channel);
    }

    @GetMapping("/{id}")
    public Rows.OrderCard card(@PathVariable long id) {
        return service.card(id);
    }

    @GetMapping("/{id}/lines")
    public List<Rows.OrderLine> lines(@PathVariable long id) {
        return service.lines(id);
    }

    @GetMapping("/{id}/amount")
    public Map<String, BigDecimal> amount(@PathVariable long id) {
        return Map.of("amount", service.amount(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created open(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                              @Valid @RequestBody Forms.OrderForm form) {
        return new Forms.Created(service.open(actor, form));
    }

    @PostMapping("/{id}/lines")
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created addLine(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                 @PathVariable long id,
                                 @Valid @RequestBody Forms.OrderLineForm form) {
        return new Forms.Created(service.addLine(actor, id, form));
    }

    @DeleteMapping("/lines/{lineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void dropLine(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                         @PathVariable long lineId) {
        service.dropLine(actor, lineId);
    }

    @PutMapping("/{id}/promo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void applyPromo(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                           @PathVariable long id,
                           @Valid @RequestBody Forms.PromoCodeForm form) {
        service.applyPromo(actor, id, form);
    }

    @PostMapping("/{id}/payment")
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created pay(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                             @PathVariable long id,
                             @Valid @RequestBody Forms.PaymentForm form) {
        return new Forms.Created(service.pay(actor, id, form));
    }

    @PostMapping("/{id}/close")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void close(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                      @PathVariable long id) {
        service.close(actor, id);
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                       @PathVariable long id) {
        service.cancel(actor, id);
    }
}
