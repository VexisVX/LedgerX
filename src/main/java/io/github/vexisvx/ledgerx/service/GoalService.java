package io.github.vexisvx.ledgerx.service;

import io.github.vexisvx.ledgerx.dao.GoalDao;
import io.github.vexisvx.ledgerx.model.Goal;
import io.github.vexisvx.ledgerx.util.Validator;

import java.math.BigDecimal;
import java.util.List;

public class GoalService {
    private final GoalDao dao = new GoalDao();

    public List<Goal> getAll() { return dao.findAll(); }

    public void create(Goal g) {
        Validator.validateGoal(g.getTargetAmount(), g.getSavedAmount(), g.getDeadline());
        dao.insert(g);
    }

    public void update(Goal g) {
        Validator.validateGoal(g.getTargetAmount(), g.getSavedAmount(), g.getDeadline());
        dao.update(g);
    }

    public void addSaving(Goal g, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than 0");
        BigDecimal newSaved = g.getSavedAmount().add(amount);
        if (newSaved.compareTo(g.getTargetAmount()) > 0)
            throw new IllegalArgumentException("Total savings cannot exceed target");
        g.setSavedAmount(newSaved);
        dao.update(g);
    }

    public void delete(long id) { dao.delete(id); }
}
