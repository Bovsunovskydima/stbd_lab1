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
import ua.lab.cafenet.service.MenuService;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService service;

    public MenuController(MenuService service) {
        this.service = service;
    }

    @GetMapping("/sections")
    public List<Rows.Section> sections() {
        return service.sections();
    }

    @PostMapping("/sections")
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created createSection(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                       @Valid @RequestBody Forms.SectionForm form) {
        return new Forms.Created(service.createSection(actor, form));
    }

    @GetMapping
    public List<Rows.MenuItem> items(@RequestParam(required = false) Integer sectionId,
                                     @RequestParam(required = false) String title) {
        return service.items(sectionId, title);
    }

    @GetMapping("/{id}")
    public Rows.MenuItem one(@PathVariable long id) {
        return service.byId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created create(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                @Valid @RequestBody Forms.MenuItemForm form) {
        return new Forms.Created(service.create(actor, form));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                       @PathVariable long id,
                       @Valid @RequestBody Forms.MenuItemForm form) {
        service.update(actor, id, form);
    }

    @PutMapping("/{id}/price")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reprice(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                        @PathVariable long id,
                        @Valid @RequestBody Forms.PriceForm form) {
        service.reprice(actor, id, form);
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

    @GetMapping("/{id}/tech-card")
    public List<Rows.TechCard> techCard(@PathVariable long id) {
        return service.techCards(id);
    }

    @PutMapping("/{id}/tech-card")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setTechCard(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                            @PathVariable long id,
                            @Valid @RequestBody Forms.TechCardForm form) {
        service.setTechCard(actor, id, form);
    }
}
