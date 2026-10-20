package io.github.vexisvx.ledgerx.model;

import io.github.vexisvx.ledgerx.ui.i18n.I18n;

public class Category {
    private Long id;
    private String name;
    private String nameEn;
    private TransactionType type;
    private String icon;

    public Category() {}
    public Category(Long id, String name, TransactionType type) {
        this.id = id; this.name = name; this.type = type;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    /**
     * Returns localized name.
     * Priority:
     *  1. i18n key "category.<name-lowercase>"
     *  2. nameEn
     *  3. name
     */
    public String displayName() {
        if (name != null && !name.isBlank()) {
            String key = "category." + name.toLowerCase().trim();
            String translated = I18n.tOrNull(key);
            if (translated != null) return translated;
        }
        if (nameEn != null && !nameEn.isBlank()) return nameEn;
        return name != null ? name : "";
    }

    @Override public String toString() { return displayName(); }
}
