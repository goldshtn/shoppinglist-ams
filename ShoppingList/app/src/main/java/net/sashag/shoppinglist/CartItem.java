package net.sashag.shoppinglist;

import java.io.Serializable;
import java.util.Date;

public class CartItem implements Serializable {

    private String title;
    private Date dueDate;
    private int amount;
    private boolean purchased;
    private long databaseId;

    public CartItem(String title, int amount) {
        this.title = title;
        this.amount = amount;
        this.dueDate = new Date();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public void updateFrom(CartItem item) {
        setTitle(item.getTitle());
        setAmount(item.getAmount());
        setDueDate(item.getDueDate());
    }

    public void setDatabaseId(long databaseId) {
        this.databaseId = databaseId;
    }

    public long getDatabaseId() {
        return databaseId;
    }

    @Override
    public String toString() {
        return String.format("%d of %s (due %s)",
                amount, title, dueDate.toString()
                );
    }
}
