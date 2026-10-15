package ua.lab.cafenet.api;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
import ua.lab.cafenet.service.StaffService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StaffService service;

    public StaffController(StaffService service) {
        this.service = service;
    }

    @GetMapping
    public List<Rows.Staff> all(@RequestParam(required = false) Integer branchId,
                                @RequestParam(required = false) String name) {
        return service.list(branchId, name);
    }

    @GetMapping("/{id}")
    public Rows.Staff one(@PathVariable long id) {
        return service.byId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created hire(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                              @Valid @RequestBody Forms.StaffForm form) {
        return new Forms.Created(service.hire(actor, form));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                       @PathVariable long id,
                       @Valid @RequestBody Forms.StaffEditForm form) {
        service.update(actor, id, form);
    }

    @PutMapping("/{id}/branch")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void move(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                     @PathVariable long id,
                     @Valid @RequestBody Forms.BranchRefForm form) {
        service.move(actor, id, form.branchId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void dismiss(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                        @PathVariable long id) {
        service.dismiss(actor, id);
    }

    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restore(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                        @PathVariable long id) {
        service.restore(actor, id);
    }

    @GetMapping("/shifts")
    public List<Rows.Shift> shifts(@RequestParam(required = false) Long staffId) {
        return service.shifts(staffId);
    }

    @PostMapping("/shifts")
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created planShift(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                   @Valid @RequestBody Forms.ShiftForm form) {
        return new Forms.Created(service.planShift(actor, form));
    }

    @GetMapping("/{id}/on-shift")
    public Map<String, Boolean> onShift(
            @PathVariable long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return Map.of("onShift", service.onShift(id, from, to));
    }
}
