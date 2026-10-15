package ua.lab.cafenet.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService service;

    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @GetMapping
    public List<Rows.Review> all(@RequestParam(required = false) Integer minRating) {
        return service.list(minRating);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created create(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                @Valid @RequestBody Forms.ReviewForm form) {
        return new Forms.Created(service.create(actor, form));
    }
}
