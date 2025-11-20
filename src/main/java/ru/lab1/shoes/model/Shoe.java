package ru.lab1.shoes.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Shoe {
	private final String name;
	private final Brand brand;
	private final int size;
	private final double price;
	private final int quantity;
	private final LocalDateTime createdAt;
	private final ProductMeta meta;
	private final List<String> tags;
	private final boolean inStock;

	public Shoe(
		String name,
		Brand brand,
		int size,
		double price,
		int quantity,
		LocalDateTime createdAt,
		ProductMeta meta,
		List<String> tags,
		boolean inStock
	) {
		this.name = Objects.requireNonNull(name, "name");
		this.brand = Objects.requireNonNull(brand, "brand");
		this.size = size;
		this.price = price;
		this.quantity = quantity;
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
		this.meta = Objects.requireNonNull(meta, "meta");
		this.tags = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(tags, "tags")));
		this.inStock = inStock;
	}

	public String getName() {
		return name;
	}

	public Brand getBrand() {
		return brand;
	}

	public int getSize() {
		return size;
	}

	public double getPrice() {
		return price;
	}

	public int getQuantity() {
		return quantity;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public ProductMeta getMeta() {
		return meta;
	}

	public List<String> getTags() {
		return tags;
	}

	public boolean isInStock() {
		return inStock;
	}

	@Override
	public String toString() {
		return "Shoe{" +
			"name='" + name + '\'' +
			", brand=" + brand +
			", size=" + size +
			", price=" + price +
			", quantity=" + quantity +
			", createdAt=" + createdAt +
			", meta=" + meta +
			", tags=" + tags +
			", inStock=" + inStock +
			'}';
	}
}



