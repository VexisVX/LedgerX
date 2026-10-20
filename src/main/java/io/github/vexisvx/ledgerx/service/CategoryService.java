package io.github.vexisvx.ledgerx.service;

import io.github.vexisvx.ledgerx.dao.CategoryDao;
import io.github.vexisvx.ledgerx.model.Category;
import io.github.vexisvx.ledgerx.model.TransactionType;
import io.github.vexisvx.ledgerx.util.Validator;

import java.util.List;

public class CategoryService {
    private final CategoryDao dao = new CategoryDao();

    public List<Category> getAll() { return dao.findAll(); }
    public List<Category> getByType(TransactionType type) { return dao.findByType(type); }

    public void create(Category c) {
        Validator.validateName(c.getName());
        dao.insert(c);
    }

    public void delete(long id) { dao.delete(id); }
}
