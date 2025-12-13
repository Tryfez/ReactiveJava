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
		long delay = 1; 
		int[] Sizes = {1000, 5000, 10000, 50000, 100000};
		
		// Храним результаты для поиска точки равенства
		long[] seqTimes = new long[Sizes.length];
		long[] parTimes = new long[Sizes.length];
		long[] seqDelayTimes = new long[Sizes.length];
		long[] parDelayTimes = new long[Sizes.length];
		
		for (int i = 0; i < Sizes.length; i++) {
			int n = Sizes[i];
			System.out.println("\n===  Количество элементов: " + n + " ===");
			List<Shoe> shoes = Generators.generateShoes(n, seed);

			// Последовательный без задержки
			Instant t1 = Instant.now();
			Map<Brand, Double> seq = Aggregations.averageSizeByBrandStream(shoes);
			Instant t2 = Instant.now();
			long seqTime = Duration.between(t1, t2).toMillis();
			seqTimes[i] = seqTime;
			System.out.println("Последовательный стрим (без задержки): " + seqTime + " ms");

			// Последовательный с задержкой
			Instant t3 = Instant.now();
			@SuppressWarnings("unused")
			Map<Brand, Double> seqDelay = Aggregations.averageSizeByBrandStreamWithDelay(shoes, delay);
			Instant t4 = Instant.now();
			long seqDelayTime = Duration.between(t3, t4).toMillis();
			seqDelayTimes[i] = seqDelayTime;
			System.out.println("Последовательный стрим (с задержкой " + delay + " мс): " + seqDelayTime + " ms");

			// Параллельный без задержки
			Instant t5 = Instant.now();
			Map<Brand, Double> par = Aggregations.averageSizeByBrandParallelStream(shoes);
			Instant t6 = Instant.now();
			long parTime = Duration.between(t5, t6).toMillis();
			parTimes[i] = parTime;
			System.out.println("Параллельный стрим (без задержки): " + parTime + " ms");

			// Параллельный с задержкой
			Instant t7 = Instant.now();
			@SuppressWarnings("unused")
			Map<Brand, Double> parDelay = Aggregations.averageSizeByBrandParallelStreamWithDelay(shoes, delay);
			Instant t8 = Instant.now();
			long parDelayTime = Duration.between(t7, t8).toMillis();
			parDelayTimes[i] = parDelayTime;
			System.out.println("Параллельный стрим (с задержкой " + delay + " мс): " + parDelayTime + " ms");

			// Параллельный с кастомным коллектором
			Instant t9 = Instant.now();
			Map<Brand, Double> parCustom = Aggregations.averageSizeByBrandParallelWithCustomCollector(shoes);
			Instant t10 = Instant.now();
			long parCustomTime = Duration.between(t9, t10).toMillis();
			System.out.println("Параллельный стрим (кастомный коллектор): " + parCustomTime + " ms");

			// Параллельный стрим с собственным Spliterator (без задержки)
			Instant t11 = Instant.now();
			Map<Brand, Double> parSpliterator = Aggregations.averageSizeByBrandWithCustomSpliterator(shoes);
			Instant t12 = Instant.now();
			long parSpliteratorTime = Duration.between(t11, t12).toMillis();
			System.out.println("Параллельный стрим (собственный Spliterator, без задержки): " + parSpliteratorTime + " ms");

			// Параллельный стрим с собственным Spliterator (с задержкой)
			Instant t13 = Instant.now();
			@SuppressWarnings("unused")
			Map<Brand, Double> parSpliteratorDelay = Aggregations.averageSizeByBrandWithCustomSpliteratorAndDelay(shoes, delay);
			Instant t14 = Instant.now();
			long parSpliteratorDelayTime = Duration.between(t13, t14).toMillis();
			System.out.println("Параллельный стрим (собственный Spliterator, с задержкой " + delay + " мс): " + parSpliteratorDelayTime + " ms");
			
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
			
			// Поиск точки равенства
			// double tolerance = 0.05;
			// if (Math.abs(seqTime - parTime) <= Math.max(seqTime, parTime) * tolerance) {
			// 	System.out.println(">>> Точка равенства производительности (без задержки, погрешность 5%): " + n + " элементов");
			// }
			// if (Math.abs(seqDelayTime - parDelayTime) <= Math.max(seqDelayTime, parDelayTime) * tolerance) {
			// 	System.out.println(">>> Точка равенства производительности (с задержкой, погрешность 5%): " + n + " элементов");
			// }
		}
		
		// Автоматический поиск точки равенства производительности
		/*
		System.out.println("\n========== Автоматический поиск точки равенства производительности ==========");
		findEqualityPoint(seed, delay, false); // без задержки
		findEqualityPointWithDelayDetailed(seed, delay);  // с задержкой - детальный поиск до 1000
		*/

	}

	/**
	 * Автоматический поиск точки равенства производительности
	 * между последовательным и параллельным стримами
	 */
	/*
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
	*/

	/**
	 *  : Детальный поиск точки равенства для случая с задержкой
	 * Проверяет диапазон до 1000 элементов с шагом 1 для точного определения
	 * ЗАКОММЕНТИРОВАНО
	 */
	/*
	private static void findEqualityPointWithDelayDetailed(long seed, long delay) {
		System.out.println("\n--- Детальный поиск точки равенства (с задержкой, диапазон 1-1000) ---");
		
		double tolerance = 0.05; // 5% погрешность
		boolean found = false;
		int bestMatch = -1;
		double bestDiff = Double.MAX_VALUE;
		
		// Проверяем малые значения с шагом 1
		for (int size = 1; size <= 1000; size++) {
			List<Shoe> shoes = Generators.generateShoes(size, seed);
			
			// Последовательный стрим с задержкой
			Instant t1 = Instant.now();
			Aggregations.averageSizeByBrandStreamWithDelay(shoes, delay);
			Instant t2 = Instant.now();
			long seqTime = Duration.between(t1, t2).toMillis();
			
			// Параллельный стрим с задержкой
			Instant t3 = Instant.now();
			Aggregations.averageSizeByBrandParallelStreamWithDelay(shoes, delay);
			Instant t4 = Instant.now();
			long parTime = Duration.between(t3, t4).toMillis();
			
			// Вычисляем относительную разницу
			double diff = Math.abs(seqTime - parTime);
			double maxTime = Math.max(seqTime, parTime);
			double relativeDiff = maxTime > 0 ? (diff / maxTime) : 0;
			
			// Сохраняем лучший результат
			if (relativeDiff < bestDiff) {
				bestDiff = relativeDiff;
				bestMatch = size;
			}
			
			// Проверяем, достигнута ли точка равенства
			if (relativeDiff <= tolerance) {
				System.out.printf(">>> Найдена точка равенства: %d элементов (seq: %d ms, par: %d ms, разница: %.2f%%)\n",
					size, seqTime, parTime, relativeDiff * 100);
				found = true;
				
				// Проверяем соседние значения для подтверждения
				if (size > 1 && size < 1000) {
					// Проверяем предыдущее значение
					List<Shoe> prevShoes = Generators.generateShoes(size - 1, seed);
					Instant t5 = Instant.now();
					Aggregations.averageSizeByBrandStreamWithDelay(prevShoes, delay);
					Instant t6 = Instant.now();
					long prevSeq = Duration.between(t5, t6).toMillis();
					
					Instant t7 = Instant.now();
					Aggregations.averageSizeByBrandParallelStreamWithDelay(prevShoes, delay);
					Instant t8 = Instant.now();
					long prevPar = Duration.between(t7, t8).toMillis();
					
					double prevDiff = Math.abs(prevSeq - prevPar) / Math.max(prevSeq, prevPar);
					
					// Проверяем следующее значение
					List<Shoe> nextShoes = Generators.generateShoes(size + 1, seed);
					Instant t9 = Instant.now();
					Aggregations.averageSizeByBrandStreamWithDelay(nextShoes, delay);
					Instant t10 = Instant.now();
					long nextSeq = Duration.between(t9, t10).toMillis();
					
					Instant t11 = Instant.now();
					Aggregations.averageSizeByBrandParallelStreamWithDelay(nextShoes, delay);
					Instant t12 = Instant.now();
					long nextPar = Duration.between(t11, t12).toMillis();
					
					double nextDiff = Math.abs(nextSeq - nextPar) / Math.max(nextSeq, nextPar);
					
					System.out.printf("   Проверка соседних значений:\n");
					System.out.printf("   %d элементов: seq=%d ms, par=%d ms, разница=%.2f%%\n",
						size - 1, prevSeq, prevPar, prevDiff * 100);
					System.out.printf("   %d элементов: seq=%d ms, par=%d ms, разница=%.2f%%\n",
						size + 1, nextSeq, nextPar, nextDiff * 100);
				}
				break;
			}
			
			// Для ускорения пропускаем некоторые значения после 100
			if (size > 100 && size % 10 != 0) continue;
		}
		
		if (!found) {
			System.out.printf(">>> Точка равенства не найдена в диапазоне 1-1000 элементов\n");
			System.out.printf("   Наименьшая разница: %.2f%% при %d элементах\n", bestDiff * 100, bestMatch);
			
			// Проверяем наилучший случай для анализа
			if (bestMatch > 0) {
				List<Shoe> bestShoes = Generators.generateShoes(bestMatch, seed);
				Instant t1 = Instant.now();
				Aggregations.averageSizeByBrandStreamWithDelay(bestShoes, delay);
				Instant t2 = Instant.now();
				long bestSeq = Duration.between(t1, t2).toMillis();
				
				Instant t3 = Instant.now();
				Aggregations.averageSizeByBrandParallelStreamWithDelay(bestShoes, delay);
				Instant t4 = Instant.now();
				long bestPar = Duration.between(t3, t4).toMillis();
			}
		}
	}
	*/
}


