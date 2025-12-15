package ru.lab1.shoes;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import ru.lab1.shoes.model.Brand;
import ru.lab1.shoes.model.Shoe;

public class Main {
	public static void main(String[] args) {
		long seed = 37L;
		
		// Сравнение производительности параллельных потоков и реактивных потоков
		System.out.println("========== Сравнение производительности ==========");
		long delay = 1; // 1 мс задержка
		int[] sizes = {500, 2000}; // Размеры для сравнения
		
		for (int n : sizes) {
			System.out.println("\n=== Количество элементов: " + n + " ===");
			List<Shoe> shoes = Generators.generateShoes(n, seed);
			
			// Параллельный стрим с задержкой
			Instant t1 = Instant.now();
			Map<Brand, Double> parDelay = Aggregations.averageSizeByBrandParallelStreamWithDelay(shoes, delay);
			Instant t2 = Instant.now();
			long parDelayTime = Duration.between(t1, t2).toMillis();
			System.out.println("Параллельный стрим (с задержкой " + delay + " мс): " + parDelayTime + " ms");
			
			// Реактивный поток Observable с задержкой
			Instant t3 = Instant.now();
			Map<Brand, Double> rxDelay = Aggregations.averageSizeByBrandObservable(shoes, delay);
			Instant t4 = Instant.now();
			long rxDelayTime = Duration.between(t3, t4).toMillis();
			System.out.println("Реактивный поток Observable (с задержкой " + delay + " мс): " + rxDelayTime + " ms");
			
			// Проверка согласованности результатов
			/*if (!parDelay.equals(rxDelay)) {
				System.out.println("Внимание: результаты отличаются!");
				System.out.println("Параллельный: " + parDelay);
				System.out.println("Реактивный: " + rxDelay);
			} else {
				System.out.println("Результаты согласованы: " + parDelay);
			}*/
			
			// Анализ производительности
			System.out.println("\n--- Анализ производительности ---");
			if (parDelayTime > 0 && rxDelayTime > 0) {
				double ratio = (double) parDelayTime / rxDelayTime;
				if (ratio > 1.0) {
					System.out.printf("Реактивный поток быстрее в %.2fx раз\n", ratio);
				} else if (ratio < 1.0) {
					System.out.printf("Параллельный поток быстрее в %.2fx раз\n", 1.0 / ratio);
				} else {
					System.out.println("Производительность одинаковая");
				}
			}
		}
		
		// Тест Flowable с backpressure для больших объемов данных
		System.out.println("\n========== Тест Flowable с backpressure (большие объемы) ==========");
		int[] largeSizes = {100000, 200000, 500000};
		
		for (int n : largeSizes) {
			System.out.println("\n=== Количество элементов: " + n + " ===");
			
			// Flowable с backpressure (генерация без задержки)
			Instant t1 = Instant.now();
			Map<Brand, Double> flowableResult = Aggregations.averageSizeByBrandFlowableWithBackpressure(n, seed);
			Instant t2 = Instant.now();
			long flowableTime = Duration.between(t1, t2).toMillis();
			System.out.println("Flowable с backpressure (без задержки): " + flowableTime + " ms");
			
			// Для сравнения: параллельный стрим без задержки
			List<Shoe> shoes = Generators.generateShoes(n, seed);
			Instant t3 = Instant.now();
			Map<Brand, Double> parNoDelay = Aggregations.averageSizeByBrandParallelStream(shoes);
			Instant t4 = Instant.now();
			long parNoDelayTime = Duration.between(t3, t4).toMillis();
			System.out.println("Параллельный стрим (без задержки, для сравнения): " + parNoDelayTime + " ms");
			
			// Проверка согласованности результатов
			if (!flowableResult.equals(parNoDelay)) {
				System.out.println("Внимание: результаты Flowable и параллельного стрима отличаются!");
			} else {
				System.out.println("Результаты согласованы");
			}
			
			// Проверка стабильности
			if (flowableTime > 0 && flowableTime < 60000) { // Меньше минуты
				System.out.println("✓ Система работает стабильно");
			} else {
				System.out.println("⚠ Внимание: возможны проблемы с производительностью");
			}
		}
	}
}


