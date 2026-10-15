package ua.lab.cafenet.service;

import org.springframework.stereotype.Service;
import ua.lab.cafenet.api.Forms;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.NotFoundException;
import ua.lab.cafenet.persistence.UnitOfWorkFactory;

import java.util.List;

@Service
public class NetworkService {

    private final UnitOfWorkFactory factory;

    public NetworkService(UnitOfWorkFactory factory) {
        this.factory = factory;
    }

    public List<Rows.Branch> branches() {
        return factory.readOnly(work -> work.branches().all());
    }

    public Rows.Branch branch(int id) {
        return factory.readOnly(work -> work.branches().byId(id))
                .orElseThrow(() -> NotFoundException.of("Філію", id));
    }

    public int create(Long actor, Forms.BranchForm form) {
        return factory.inTransaction(actor, work -> work.branches()
                .add(form.name(), form.city(), form.address(), form.opensAt(), form.closesAt()));
    }

    public void update(Long actor, int id, Forms.BranchForm form) {
        factory.run(actor, work -> work.branches()
                .edit(id, form.name(), form.city(), form.address(), form.opensAt(), form.closesAt()));
    }

    public List<Rows.Position> positions() {
        return factory.readOnly(work -> work.branches().positions());
    }

    public int createPosition(Long actor, Forms.PositionForm form) {
        return factory.inTransaction(actor, work -> work.branches().addPosition(form.title(), form.baseRate()));
    }
}
