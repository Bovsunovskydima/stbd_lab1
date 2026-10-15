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
import ua.lab.cafenet.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<Rows.Product> all(@RequestParam(required = false) String name) {
        return service.list(name);
    }

    @GetMapping("/{id}")
    public Rows.Product one(@PathVariable long id) {
        return service.byId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created create(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                @Valid @RequestBody Forms.ProductForm form) {
        return new Forms.Created(service.create(actor, form));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                       @PathVariable long id,
                       @Valid @RequestBody Forms.ProductForm form) {
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
}
