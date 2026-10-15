package ua.lab.cafenet.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.service.SupplyService;

import java.util.List;

@RestController
@RequestMapping("/api/supply")
public class SupplyController {

    private final SupplyService service;

    public SupplyController(SupplyService service) {
        this.service = service;
    }

    @GetMapping("/suppliers")
    public List<Rows.Supplier> suppliers() {
        return service.suppliers();
    }

    @PostMapping("/suppliers")
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created createSupplier(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                        @Valid @RequestBody Forms.SupplierForm form) {
        return new Forms.Created(service.createSupplier(actor, form));
    }

    @GetMapping("/purchases")
    public List<Rows.Purchase> purchases(@RequestParam(required = false) Integer branchId,
                                         @RequestParam(required = false) String status) {
        return service.purchases(branchId, status);
    }

    @GetMapping("/purchases/{id}")
    public Rows.Purchase purchase(@PathVariable long id) {
        return service.purchase(id);
    }

    @GetMapping("/purchases/{id}/lines")
    public List<Rows.PurchaseLine> lines(@PathVariable long id) {
        return service.lines(id);
    }

    @PostMapping("/purchases")
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created open(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                              @Valid @RequestBody Forms.PurchaseForm form) {
        return new Forms.Created(service.open(actor, form));
    }

    @PostMapping("/purchases/{id}/lines")
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created addLine(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                 @PathVariable long id,
                                 @Valid @RequestBody Forms.PurchaseLineForm form) {
        return new Forms.Created(service.addLine(actor, id, form));
    }

    @PostMapping("/purchases/{id}/receive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receive(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                        @PathVariable long id) {
        service.receive(actor, id);
    }

    @PostMapping("/purchases/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                       @PathVariable long id) {
        service.cancel(actor, id);
    }
}
