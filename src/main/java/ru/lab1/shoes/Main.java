package ru.lab1.shoes;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import ru.lab1.shoes.model.Brand;
import ru.lab1.shoes.model.Shoe;
import ru.lab1.shoes.model.Sale;
import ru.lab1.shoes.model.Supply;

public class Main {
	public static void main(String[] args) {
		int[] sizes = {5000, 50000, 250000};
		long seed = 37L;
		List<Sale> sales = Generators.generateSales(100, seed);
		List<Supply> supplies = Generators.generateSupplies(50, seed);
		for (int n : sizes) {
			System.out.println("=== Количество элементов: " + n + " ===");
			List<Shoe> shoes = Generators.generateShoes(n, seed);

			shoes.stream().limit(3).forEach(s -> System.out.println("Пример: " + s));

			// Итерационно
			Instant t1 = Instant.now();
			Map<Brand, Double> r1 = Aggregations.averageSizeByBrandIterative(shoes);
			Instant t2 = Instant.now();
			System.out.println("Итерационно: " + Duration.between(t1, t2).toMillis() + " ms");

			// Стандартные коллекторы
			Instant t3 = Instant.now();
			Map<Brand, Double> r2 = Aggregations.averageSizeByBrandStream(shoes);
			Instant t4 = Instant.now();
			System.out.println("Стандартный коллектор: " + Duration.between(t3, t4).toMillis() + " ms");

			// Кастомный коллектор
			Instant t5 = Instant.now();
			Map<Brand, Double> r3 = Aggregations.averageSizeByBrandWithCustomCollector(shoes);
			Instant t6 = Instant.now();
			System.out.println("Кастомный коллектор: " + Duration.between(t5, t6).toMillis() + " ms");

			// Проверка согласованности результатов
			if (!r1.equals(r2) || !r2.equals(r3)) {
				System.out.println("Внимание: результаты отличаются!");
				System.out.println("Итерационно: " + r1);
				System.out.println("Стандартные: " + r2);
				System.out.println("Кастомный: " + r3);
			} else {
				System.out.println("Средний размер по брендам: " + r1);
			}
			System.out.println();
		}
		// Проверка продаж и поставок
//		System.out.println("Сгенерировано продаж: " + sales.size());
//		System.out.println("Сгенерировано поставок: " + supplies.size());
	}
}


