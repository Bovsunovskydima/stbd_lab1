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
import ua.lab.cafenet.service.ClientService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @GetMapping
    public List<Rows.Client> all(@RequestParam(required = false) String name) {
        return service.list(name);
    }

    @GetMapping("/{id}")
    public Rows.Client one(@PathVariable long id) {
        return service.byId(id);
    }

    @GetMapping("/{id}/spent")
    public Map<String, BigDecimal> spent(@PathVariable long id) {
        return Map.of("spent", service.spent(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created create(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                @Valid @RequestBody Forms.ClientForm form) {
        return new Forms.Created(service.create(actor, form));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                       @PathVariable long id,
                       @Valid @RequestBody Forms.ClientForm form) {
        service.update(actor, id, form);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                       @PathVariable long id) {
        service.remove(actor, id);
    }

    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restore(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                        @PathVariable long id) {
        service.restore(actor, id);
    }

    @GetMapping("/{id}/addresses")
    public List<Rows.Address> addresses(@PathVariable long id) {
        return service.addresses(id);
    }

    @PostMapping("/{id}/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created addAddress(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                    @PathVariable long id,
                                    @Valid @RequestBody Forms.AddressForm form) {
        return new Forms.Created(service.addAddress(actor, id, form));
    }

    @PutMapping("/addresses/{addressId}/default")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void makeDefault(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                            @PathVariable long addressId) {
        service.makeDefault(actor, addressId);
    }

    @GetMapping("/{id}/card")
    public Rows.LoyaltyCard card(@PathVariable long id) {
        return service.card(id);
    }

    @PostMapping("/{id}/card")
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created issueCard(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                   @PathVariable long id) {
        return new Forms.Created(service.issueCard(actor, id));
    }

    @PutMapping("/{id}/card/points")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void adjustPoints(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                             @PathVariable long id,
                             @Valid @RequestBody Forms.PointsForm form) {
        service.adjustPoints(actor, id, form.points());
    }
}
