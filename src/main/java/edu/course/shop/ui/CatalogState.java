package edu.course.shop.ui;
/** Retains the shopper's filters between screens. */
public final class CatalogState {
    private String minimumPrice="",maximumPrice="";
    public String getMinimumPrice(){return minimumPrice;}
    public String getMaximumPrice(){return maximumPrice;}
    public void setPriceRange(String minimum,String maximum){minimumPrice=minimum;maximumPrice=maximum;}
    private int categoryIndex, sortIndex, budgetIndex;
    private boolean inStockOnly, filtersExpanded;
    private double scrollPosition;
    public int getCategoryIndex() { return categoryIndex; }
    public void setCategoryIndex(int value) { categoryIndex=value; }
    public int getSortIndex() { return sortIndex; }
    public void setSortIndex(int value) { sortIndex=value; }
    public int getBudgetIndex() { return budgetIndex; }
    public void setBudgetIndex(int value) { budgetIndex=value; }
    public boolean isInStockOnly() { return inStockOnly; }
    public void setInStockOnly(boolean value) { inStockOnly=value; }
    public boolean isFiltersExpanded() { return filtersExpanded; }
    public void setFiltersExpanded(boolean value) { filtersExpanded=value; }
    public double getScrollPosition() { return scrollPosition; }
    public void setScrollPosition(double value) { scrollPosition=value; }
}
