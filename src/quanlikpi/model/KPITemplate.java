package quanlikpi.model;

public class KPITemplate {
    private int id;
    private String name;
    private String description;
    private String unit;
    private double defaultWeight;
    private boolean isActive;

    public KPITemplate() {
    }

    public KPITemplate(int id, String name, String description, String unit, double defaultWeight, boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.unit = unit;
        this.defaultWeight = defaultWeight;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getDefaultWeight() {
        return defaultWeight;
    }

    public void setDefaultWeight(double defaultWeight) {
        this.defaultWeight = defaultWeight;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return name;
    }
}
