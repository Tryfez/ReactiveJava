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
		// LAB1: Оригинальные тесты (закомментировано для Lab 2)
		/*
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
		*/
		
		long seed = 37L;

		// LAB2: Тесты с задержкой и параллельными стримами
		System.out.println("\n========== LAB2: Тесты с задержкой и параллельными стримами ==========");
		long delay = 1; // 1 мс задержка
		int[] lab2Sizes = {1000, 5000, 10000, 50000, 100000};
		
		// Храним результаты для поиска точки равенства
		long[] seqTimes = new long[lab2Sizes.length];
		long[] parTimes = new long[lab2Sizes.length];
		long[] seqDelayTimes = new long[lab2Sizes.length];
		long[] parDelayTimes = new long[lab2Sizes.length];
		
		for (int i = 0; i < lab2Sizes.length; i++) {
			int n = lab2Sizes[i];
			System.out.println("\n=== LAB2: Количество элементов: " + n + " ===");
			List<Shoe> shoes = Generators.generateShoes(n, seed);

			// Последовательный стрим без задержки
			Instant t1 = Instant.now();
			Map<Brand, Double> seq = Aggregations.averageSizeByBrandStream(shoes);
			Instant t2 = Instant.now();
			long seqTime = Duration.between(t1, t2).toMillis();
			seqTimes[i] = seqTime;
			System.out.println("Последовательный стрим (без задержки): " + seqTime + " ms");

			// Последовательный стрим с задержкой
			Instant t3 = Instant.now();
			@SuppressWarnings("unused")
			Map<Brand, Double> seqDelay = Aggregations.averageSizeByBrandStreamWithDelay(shoes, delay);
			Instant t4 = Instant.now();
			long seqDelayTime = Duration.between(t3, t4).toMillis();
			seqDelayTimes[i] = seqDelayTime;
			System.out.println("Последовательный стрим (с задержкой " + delay + " мс): " + seqDelayTime + " ms");

			// Параллельный стрим без задержки
			Instant t5 = Instant.now();
			Map<Brand, Double> par = Aggregations.averageSizeByBrandParallelStream(shoes);
			Instant t6 = Instant.now();
			long parTime = Duration.between(t5, t6).toMillis();
			parTimes[i] = parTime;
			System.out.println("Параллельный стрим (без задержки): " + parTime + " ms");

			// Параллельный стрим с задержкой
			Instant t7 = Instant.now();
			@SuppressWarnings("unused")
			Map<Brand, Double> parDelay = Aggregations.averageSizeByBrandParallelStreamWithDelay(shoes, delay);
			Instant t8 = Instant.now();
			long parDelayTime = Duration.between(t7, t8).toMillis();
			parDelayTimes[i] = parDelayTime;
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
			
			// Поиск точки равенства производительности (с погрешностью 5%)
			double tolerance = 0.05;
			if (Math.abs(seqTime - parTime) <= Math.max(seqTime, parTime) * tolerance) {
				System.out.println(">>> Точка равенства производительности (без задержки, погрешность 5%): " + n + " элементов");
			}
			if (Math.abs(seqDelayTime - parDelayTime) <= Math.max(seqDelayTime, parDelayTime) * tolerance) {
				System.out.println(">>> Точка равенства производительности (с задержкой, погрешность 5%): " + n + " элементов");
			}
		}
		
		// Автоматический поиск точки равенства производительности
		System.out.println("\n========== LAB2: Автоматический поиск точки равенства производительности ==========");
		findEqualityPoint(seed, delay, false); // без задержки
		findEqualityPoint(seed, delay, true);  // с задержкой

		System.out.println("\n========== Для детальных JMH бенчмарков запустите: ==========");
		System.out.println("mvn clean package");
		System.out.println("java -jar target/benchmarks.jar");
	}

	/**
	 * LAB2: Автоматический поиск точки равенства производительности
	 * между последовательным и параллельным стримами
	 */
	private static void findEqualityPoint(long seed, long delay, boolean withDelay) {
		System.out.println("\n--- Поиск точки равенства (" + (withDelay ? "с задержкой" : "без задержки") + ") ---");
		
		// Начальный диапазон для поиска
		int minSize = 100;
		int maxSize = 500000;
		int currentSize = minSize;
		double tolerance = 0.05; // 5% погрешность
		
		long prevSeqTime = 0;
		long prevParTime = 0;
		boolean found = false;
		int iterations = 0;
		int maxIterations = 50; // Защита от бесконечного цикла
		
		while (currentSize <= maxSize && iterations < maxIterations) {
			iterations++;
			List<Shoe> shoes = Generators.generateShoes(currentSize, seed);
			
			long seqTime, parTime;
			
			if (withDelay) {
				// С задержкой
				Instant t1 = Instant.now();
				Aggregations.averageSizeByBrandStreamWithDelay(shoes, delay);
				Instant t2 = Instant.now();
				seqTime = Duration.between(t1, t2).toMillis();
				
				Instant t3 = Instant.now();
				Aggregations.averageSizeByBrandParallelStreamWithDelay(shoes, delay);
				Instant t4 = Instant.now();
				parTime = Duration.between(t3, t4).toMillis();
			} else {
				// Без задержки
				Instant t1 = Instant.now();
				Aggregations.averageSizeByBrandStream(shoes);
				Instant t2 = Instant.now();
				seqTime = Duration.between(t1, t2).toMillis();
				
				Instant t3 = Instant.now();
				Aggregations.averageSizeByBrandParallelStream(shoes);
				Instant t4 = Instant.now();
				parTime = Duration.between(t3, t4).toMillis();
			}
			
			// Проверяем, пересекли ли мы точку равенства
			if (iterations > 1) {
				boolean prevSeqFaster = prevSeqTime < prevParTime;
				boolean currSeqFaster = seqTime < parTime;
				
				// Если произошло пересечение (было seq < par, стало seq >= par или наоборот)
				if (prevSeqFaster != currSeqFaster || 
				    Math.abs(seqTime - parTime) <= Math.max(seqTime, parTime) * tolerance) {
					
					// Уточняем поиск в меньшем диапазоне
					int step = Math.max(1, currentSize / 10);
					int lowerBound = currentSize - step * 2;
					int upperBound = currentSize;
					
					for (int size = lowerBound; size <= upperBound && size > 0; size += step / 2) {
						List<Shoe> testShoes = Generators.generateShoes(size, seed);
						long testSeqTime, testParTime;
						
						if (withDelay) {
							Instant t1 = Instant.now();
							Aggregations.averageSizeByBrandStreamWithDelay(testShoes, delay);
							Instant t2 = Instant.now();
							testSeqTime = Duration.between(t1, t2).toMillis();
							
							Instant t3 = Instant.now();
							Aggregations.averageSizeByBrandParallelStreamWithDelay(testShoes, delay);
							Instant t4 = Instant.now();
							testParTime = Duration.between(t3, t4).toMillis();
						} else {
							Instant t1 = Instant.now();
							Aggregations.averageSizeByBrandStream(testShoes);
							Instant t2 = Instant.now();
							testSeqTime = Duration.between(t1, t2).toMillis();
							
							Instant t3 = Instant.now();
							Aggregations.averageSizeByBrandParallelStream(testShoes);
							Instant t4 = Instant.now();
							testParTime = Duration.between(t3, t4).toMillis();
						}
						
						if (Math.abs(testSeqTime - testParTime) <= Math.max(testSeqTime, testParTime) * tolerance) {
							System.out.printf(">>> Найдена точка равенства: %d элементов (seq: %d ms, par: %d ms, разница: %.1f%%)\n",
								size, testSeqTime, testParTime, 
								100.0 * Math.abs(testSeqTime - testParTime) / Math.max(testSeqTime, testParTime));
							found = true;
							break;
						}
					}
					
					if (found) break;
				}
			}
			
			prevSeqTime = seqTime;
			prevParTime = parTime;
			
			// Увеличиваем размер для следующей итерации
			if (seqTime < parTime) {
				// Последовательный быстрее - увеличиваем размер
				currentSize = (int) (currentSize * 1.5);
			} else {
				// Параллельный быстрее или равны - уменьшаем размер
				currentSize = (int) (currentSize * 0.8);
			}
			
			// Защита от слишком маленьких значений
			if (currentSize < 10) currentSize = 10;
		}
		
		if (!found) {
			System.out.println(">>> Точка равенства не найдена в диапазоне " + minSize + " - " + maxSize + " элементов");
			System.out.println("   Возможно, параллельный стрим всегда быстрее/медленнее для данного случая");
		}
	}
}


