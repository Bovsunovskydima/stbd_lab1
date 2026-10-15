package ua.lab.cafenet.service;

import org.springframework.stereotype.Service;
import ua.lab.cafenet.api.Forms;
import ua.lab.cafenet.domain.Rows;
import ua.lab.cafenet.persistence.NotFoundException;
import ua.lab.cafenet.persistence.UnitOfWorkFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StaffService {

    private final UnitOfWorkFactory factory;

    public StaffService(UnitOfWorkFactory factory) {
        this.factory = factory;
    }

    public List<Rows.Staff> list(Integer branchId, String name) {
        return factory.readOnly(work -> work.staff().search(branchId, name));
    }

    public Rows.Staff byId(long id) {
        return factory.readOnly(work -> work.staff().byId(id))
                .orElseThrow(() -> NotFoundException.of("Працівника", id));
    }

    public long hire(Long actor, Forms.StaffForm form) {
        return factory.inTransaction(actor, work -> work.staff()
                .hire(form.branchId(), form.positionId(), form.fullName(), form.phone(), form.hiredOn()));
    }

    public void update(Long actor, long id, Forms.StaffEditForm form) {
        factory.run(actor, work -> work.staff().edit(id, form.positionId(), form.phone()));
    }

    public void move(Long actor, long id, int branchId) {
        factory.run(actor, work -> work.staff().move(id, branchId));
    }

    public void dismiss(Long actor, long id) {
        factory.run(actor, work -> work.staff().dismiss(id));
    }

    public void restore(Long actor, long id) {
        factory.run(actor, work -> work.staff().restore(id));
    }

    public List<Rows.Shift> shifts(Long staffId) {
        return factory.readOnly(work -> work.staff().shifts(staffId));
    }

    public long planShift(Long actor, Forms.ShiftForm form) {
        return factory.inTransaction(actor, work -> work.staff()
                .planShift(form.staffId(), form.startsAt(), form.endsAt()));
    }

    public boolean onShift(long staffId, LocalDateTime from, LocalDateTime to) {
        return factory.readOnly(work -> work.staff().onShift(staffId, from, to));
    }
}
