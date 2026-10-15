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
import ua.lab.cafenet.service.PromoService;

import java.util.List;

@RestController
@RequestMapping("/api/promos")
public class PromoController {

    private final PromoService service;

    public PromoController(PromoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Rows.Promo> all(@RequestParam(required = false) Boolean onlyValid) {
        return service.list(onlyValid);
    }

    @GetMapping("/{id}")
    public Rows.Promo one(@PathVariable int id) {
        return service.byId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created create(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                @Valid @RequestBody Forms.PromoForm form) {
        return new Forms.Created(service.create(actor, form));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                       @PathVariable int id,
                       @Valid @RequestBody Forms.PromoEditForm form) {
        service.update(actor, id, form);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                       @PathVariable int id) {
        service.remove(actor, id);
    }

    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restore(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                        @PathVariable int id) {
        service.restore(actor, id);
    }
}
