package ru.lab1.shoes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import ru.lab1.shoes.model.Brand;
import ru.lab1.shoes.model.ProductMeta;
import ru.lab1.shoes.model.Sale;
import ru.lab1.shoes.model.Shoe;
import ru.lab1.shoes.model.Supply;

public final class Generators {
	private static final String[] MODEL_NAMES = {
		"Air Runner", "Street Pro", "Urban Flex", "Trail Max", "Court One", "Daily Walk", "Marathoner", "PowerLift"
	};
	private static final String[] TAGS = {
		"men", "women", "kids", "running", "casual", "basketball", "tennis", "outdoor", "leather", "canvas"
	};

	private Generators() {}

	public static List<Shoe> generateShoes(int count, long seed) {
		Random rnd = new Random(seed);
		List<Shoe> list = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			String name = MODEL_NAMES[rnd.nextInt(MODEL_NAMES.length)] + " " + (100 + rnd.nextInt(900));
			Brand brand = Brand.values()[rnd.nextInt(Brand.values().length)];
			int size = 35 + rnd.nextInt(15); 
			double price = round2(3000.00 + rnd.nextDouble() * 25000.0);
			int quantity = rnd.nextInt(0, 200);
			LocalDateTime createdAt = randomDateTime(rnd);
			ProductMeta meta = new ProductMeta(brand.name() + " Inc");
			List<String> tags = randomTags(rnd);
			boolean inStock = quantity > 0;
			list.add(new Shoe(name, brand, size, price, quantity, createdAt, meta, tags, inStock));
		}
		return list;
	}

	public static List<Sale> generateSales(int count, long seed) {
		Random rnd = new Random(seed ^ 0x9E3779B97F4A7C15L);
		List<Sale> list = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			String itemName = MODEL_NAMES[rnd.nextInt(MODEL_NAMES.length)].toLowerCase(Locale.ROOT);
			int quantity = 1 + rnd.nextInt(5);
			double totalPrice = round2(29.99 + rnd.nextDouble() * 500.0);
			LocalDateTime dateTime = randomDateTime(rnd);
			list.add(new Sale(itemName, quantity, totalPrice, dateTime));
		}
		return list;
	}

	public static List<Supply> generateSupplies(int count, long seed) {
		Random rnd = new Random(seed ^ 0xC2B2AE3D27D4EB4FL);
		List<Supply> list = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			LocalDateTime dateTime = randomDateTime(rnd);
			String itemName = MODEL_NAMES[rnd.nextInt(MODEL_NAMES.length)];
			int quantity = 50 + rnd.nextInt(500);
			double amount = round2(1000.0 + rnd.nextDouble() * 20000.0);
			String supplierName = brandNameLike(rnd) + " Inc";
			list.add(new Supply(dateTime, itemName, quantity, amount, supplierName));
		}
		return list;
	}

	private static List<String> randomTags(Random rnd) {
		int n = 1 + rnd.nextInt(4);
		List<String> res = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			res.add(TAGS[rnd.nextInt(TAGS.length)]);
		}
		return res;
	}

	private static LocalDateTime randomDateTime(Random rnd) {
		long now = System.currentTimeMillis();
		long twoYears = 2L * 365 * 24 * 60 * 60 * 1000;
		long millis = now - rnd.nextLong(twoYears);
		return LocalDateTime.ofEpochSecond(millis / 1000, 0, ZoneOffset.UTC);
	}

	private static double round2(double v) {
		return Math.round(v * 100.0) / 100.0;
	}

	private static String brandNameLike(Random rnd) {
		Brand b = Brand.values()[rnd.nextInt(Brand.values().length)];
		return b.name();
	}
}


