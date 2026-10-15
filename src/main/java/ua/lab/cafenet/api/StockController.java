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
import ua.lab.cafenet.service.StockService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService service;

    public StockController(StockService service) {
        this.service = service;
    }

    @GetMapping
    public List<Rows.StockBalance> balances(@RequestParam(required = false) Integer branchId) {
        return service.balances(branchId);
    }

    @GetMapping("/low")
    public List<Rows.StockBalance> low() {
        return service.low();
    }

    @GetMapping("/balance")
    public Map<String, BigDecimal> balance(@RequestParam int branchId, @RequestParam long productId) {
        return Map.of("balance", service.balance(branchId, productId));
    }

    @PostMapping("/movements")
    @ResponseStatus(HttpStatus.CREATED)
    public Forms.Created correct(@RequestHeader(value = "X-Staff-Id", required = false) Long actor,
                                 @Valid @RequestBody Forms.StockForm form) {
        return new Forms.Created(service.correct(actor, form));
    }
}
