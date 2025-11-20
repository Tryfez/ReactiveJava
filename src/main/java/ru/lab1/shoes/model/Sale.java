package ru.lab1.shoes.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Sale {
	private final String itemName;
	private final int quantity;
	private final double totalPrice;
	private final LocalDateTime dateTime;

	public Sale(String itemName, int quantity, double totalPrice, LocalDateTime dateTime) {
		this.itemName = Objects.requireNonNull(itemName, "itemName");
		this.quantity = quantity;
		this.totalPrice = totalPrice;
		this.dateTime = Objects.requireNonNull(dateTime, "dateTime");
	}

	public String getItemName() {
		return itemName;
	}

	public int getQuantity() {
		return quantity;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}
}



