package ua.lab.cafenet.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.service.NetworkService;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final NetworkService service;

    public BranchController(NetworkService service) {
        this.service = service;
    }

    @GetMapping
    public List<Rows.Branch> all() {
        return service.branches();
    }

    @GetMapping("/{id}")
    public Rows.Branch one(@PathVariable int id) {
        return service.branch(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created create(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                @Valid @RequestBody Forms.BranchForm form) {
        return new Forms.Created(service.create(actor, form));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                       @PathVariable int id,
                       @Valid @RequestBody Forms.BranchForm form) {
        service.update(actor, id, form);
    }

    @GetMapping("/positions")
    public List<Rows.Position> positions() {
        return service.positions();
    }

    @PostMapping("/positions")
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created createPosition(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                        @Valid @RequestBody Forms.PositionForm form) {
        return new Forms.Created(service.createPosition(actor, form));
    }
}
