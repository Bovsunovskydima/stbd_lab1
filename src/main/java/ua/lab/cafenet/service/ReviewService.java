package ua.lab.cafenet.service;

import org.springframework.stereotype.Service;
import ua.lab.cafenet.api.Forms;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.UnitOfWorkFactory;

import java.util.List;

@Service
public class ReviewService {

    private final UnitOfWorkFactory factory;

    public ReviewService(UnitOfWorkFactory factory) {
        this.factory = factory;
    }

    public List<Rows.Review> list(Integer minRating) {
        return factory.readOnly(work -> work.reviews().all(minRating));
    }

    public long create(Long actor, Forms.ReviewForm form) {
        return factory.inTransaction(actor, work -> work.reviews()
                .add(form.orderId(), form.rating(), form.comment()));
    }
}
