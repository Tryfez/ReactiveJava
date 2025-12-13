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
		// LAB1: Оригинальные тесты
		System.out.println("========== LAB1: Оригинальные тесты ==========");
		int[] sizes = {5000, 50000, 250000};
		long seed = 37L;
		// Переменные для возможного использования в будущем
		@SuppressWarnings("unused")
		List<Sale> sales = Generators.generateSales(100, seed);
		@SuppressWarnings("unused")
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

		// LAB2: Тесты с задержкой и параллельными стримами
		System.out.println("\n========== LAB2: Тесты с задержкой и параллельными стримами ==========");
		long delay = 1; // 1 мс задержка
		int[] lab2Sizes = {1000, 5000, 10000, 50000, 100000};
		
		for (int n : lab2Sizes) {
			System.out.println("\n=== LAB2: Количество элементов: " + n + " ===");
			List<Shoe> shoes = Generators.generateShoes(n, seed);

			// Последовательный стрим без задержки
			Instant t1 = Instant.now();
			Map<Brand, Double> seq = Aggregations.averageSizeByBrandStream(shoes);
			Instant t2 = Instant.now();
			long seqTime = Duration.between(t1, t2).toMillis();
			System.out.println("Последовательный стрим (без задержки): " + seqTime + " ms");

			// Последовательный стрим с задержкой
			Instant t3 = Instant.now();
			Map<Brand, Double> seqDelay = Aggregations.averageSizeByBrandStreamWithDelay(shoes, delay);
			Instant t4 = Instant.now();
			long seqDelayTime = Duration.between(t3, t4).toMillis();
			System.out.println("Последовательный стрим (с задержкой " + delay + " мс): " + seqDelayTime + " ms");

			// Параллельный стрим без задержки
			Instant t5 = Instant.now();
			Map<Brand, Double> par = Aggregations.averageSizeByBrandParallelStream(shoes);
			Instant t6 = Instant.now();
			long parTime = Duration.between(t5, t6).toMillis();
			System.out.println("Параллельный стрим (без задержки): " + parTime + " ms");

			// Параллельный стрим с задержкой
			Instant t7 = Instant.now();
			Map<Brand, Double> parDelay = Aggregations.averageSizeByBrandParallelStreamWithDelay(shoes, delay);
			Instant t8 = Instant.now();
			long parDelayTime = Duration.between(t7, t8).toMillis();
			System.out.println("Параллельный стрим (с задержкой " + delay + " мс): " + parDelayTime + " ms");

			// Параллельный стрим с кастомным коллектором
			Instant t9 = Instant.now();
			Map<Brand, Double> parCustom = Aggregations.averageSizeByBrandParallelWithCustomCollector(shoes);
			Instant t10 = Instant.now();
			long parCustomTime = Duration.between(t9, t10).toMillis();
			System.out.println("Параллельный стрим (кастомный коллектор): " + parCustomTime + " ms");

			// Параллельный стрим с собственным Spliterator
			Instant t11 = Instant.now();
			Map<Brand, Double> parSpliterator = Aggregations.averageSizeByBrandWithCustomSpliterator(shoes);
			Instant t12 = Instant.now();
			long parSpliteratorTime = Duration.between(t11, t12).toMillis();
			System.out.println("Параллельный стрим (собственный Spliterator): " + parSpliteratorTime + " ms");

			// Проверка согласованности результатов (LAB2)
			if (!seq.equals(par) || !par.equals(parCustom) || !parCustom.equals(parSpliterator)) {
				System.out.println("Внимание: результаты параллельных методов отличаются!");
			}

			// Анализ результатов
			System.out.println("\n--- Анализ производительности ---");
			if (seqTime > 0 && parTime > 0) {
				double speedup = (double) seqTime / parTime;
				System.out.printf("Ускорение (параллельный/последовательный без задержки): %.2fx\n", speedup);
			}
			if (seqDelayTime > 0 && parDelayTime > 0) {
				double speedupDelay = (double) seqDelayTime / parDelayTime;
				System.out.printf("Ускорение (параллельный/последовательный с задержкой): %.2fx\n", speedupDelay);
			}
			
			// Поиск точки равенства производительности
			if (seqTime == parTime) {
				System.out.println(">>> Точка равенства производительности (без задержки): " + n + " элементов");
			}
			if (seqDelayTime == parDelayTime) {
				System.out.println(">>> Точка равенства производительности (с задержкой): " + n + " элементов");
			}
		}

		System.out.println("\n========== Для детальных JMH бенчмарков запустите: ==========");
		System.out.println("mvn clean package");
		System.out.println("java -jar target/benchmarks.jar");
	}
}


