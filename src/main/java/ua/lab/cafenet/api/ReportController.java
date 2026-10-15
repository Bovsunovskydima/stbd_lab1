package ua.lab.cafenet.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.service.ReportService;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/revenue")
    public List<Rows.RevenueDay> revenue(@RequestParam(required = false) Integer branchId) {
        return service.revenue(branchId);
    }

    @GetMapping("/top-menu")
    public List<Rows.MenuTop> topMenu() {
        return service.topMenu();
    }

    @GetMapping("/couriers")
    public List<Rows.CourierStat> couriers() {
        return service.couriers();
    }

    @GetMapping("/low-stock")
    public List<Rows.StockBalance> lowStock() {
        return service.lowStock();
    }
}
