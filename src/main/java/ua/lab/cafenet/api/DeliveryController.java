package ua.lab.cafenet.api;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
import ua.lab.cafenet.service.DeliveryService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService service;

    public DeliveryController(DeliveryService service) {
        this.service = service;
    }

    @GetMapping
    public List<Rows.Delivery> all(@RequestParam(required = false) Boolean pending) {
        return service.list(pending);
    }

    @GetMapping("/{id}")
    public Rows.Delivery one(@PathVariable long id) {
        return service.byId(id);
    }

    @PostMapping("/orders/{orderId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created create(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                @PathVariable long orderId,
                                @Valid @RequestBody Forms.DeliveryForm form) {
        return new Forms.Created(service.create(actor, orderId, form));
    }

    @PutMapping("/{id}/courier")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assign(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                       @PathVariable long id,
                       @Valid @RequestBody Forms.AssignForm form) {
        service.assign(actor, id, form);
    }

    @PostMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void complete(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                         @PathVariable long id) {
        service.complete(actor, id);
    }

    @GetMapping("/couriers")
    public List<Rows.CourierStat> couriers() {
        return service.couriers();
    }

    @PostMapping("/couriers")
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created addCourier(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                    @Valid @RequestBody Forms.CourierForm form) {
        return new Forms.Created(service.addCourier(actor, form));
    }

    @GetMapping("/couriers/free")
    public List<Rows.FreeCourier> free(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return service.free(from, to);
    }
}
