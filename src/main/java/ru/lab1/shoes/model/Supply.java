package ru.lab1.shoes.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Supply {
	private final LocalDateTime dateTime;
	private final String itemName;
	private final int quantity;
	private final double totalAmount;
	private final String supplierName;

	public Supply(LocalDateTime dateTime, String itemName, int quantity, double totalAmount, String supplierName) {
		this.dateTime = Objects.requireNonNull(dateTime, "dateTime");
		this.itemName = Objects.requireNonNull(itemName, "itemName");
		this.quantity = quantity;
		this.totalAmount = totalAmount;
		this.supplierName = Objects.requireNonNull(supplierName, "supplierName");
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public String getItemName() {
		return itemName;
	}

	public int getQuantity() {
		return quantity;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public String getSupplierName() {
		return supplierName;
	}
}



