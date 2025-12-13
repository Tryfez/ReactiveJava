package ru.lab1.shoes;

import java.util.HashMap; 
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.Spliterator;
import java.util.List;

import ru.lab1.shoes.model.Brand;
import ru.lab1.shoes.model.Shoe;

public final class Aggregations {
	private Aggregations() {}

	// Итерационно
	public static Map<Brand, Double> averageSizeByBrandIterative(Iterable<Shoe> shoes) {
		Map<Brand, long[]> tmp = new HashMap<>();
		// Map<Brand, long[]> tmp = new EnumMapLike<>();
		for (Shoe s : shoes){
			Brand b = s.getBrand();
			long[] sc = tmp.computeIfAbsent(b, k -> new long[2]);
			sc[0] += s.getSize();
			sc[1] += 1;
		}
		Map<Brand, Double> result = new HashMap<>();
		// Map<Brand, Double> result = new EnumMapLike<>();
		for (Map.Entry<Brand, long[]> e : tmp.entrySet()) {
			long sum = e.getValue()[0];
			long cnt = e.getValue()[1];
			result.put(e.getKey(), cnt == 0 ? 0.0 : (double) sum / cnt);
		}
		return result;
	}

	// Стандартные коллекторы
	public static Map<Brand, Double> averageSizeByBrandStream(java.util.Collection<Shoe> shoes) {
		return shoes.stream()
			.collect(Collectors.groupingBy(
				Shoe::getBrand,
				Collectors.averagingInt(Shoe::getSize)
			));
	}

	// Кастомный 
	public static Map<Brand, Double> averageSizeByBrandWithCustomCollector(java.util.Collection<Shoe> shoes) {
		return shoes.stream().collect(new AvgSizeByBrandCollector());
	}

	// ========== LAB2: Методы с задержкой ==========
	
	/**
	 * LAB2: Получить бренд с задержкой (имитация получения из БД)
	 */
	public static Brand getBrand(Shoe shoe, long delay) {
		if (delay > 0) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return shoe.getBrand();
	}

	/**
	 * LAB2: Стандартный стрим с задержкой получения бренда
	 */
	public static Map<Brand, Double> averageSizeByBrandStreamWithDelay(
		java.util.Collection<Shoe> shoes, long delay) {
		return shoes.stream()
			.collect(Collectors.groupingBy(
				shoe -> getBrand(shoe, delay),
				Collectors.averagingInt(Shoe::getSize)
			));
	}

	// ========== LAB2: Параллельные стримы ==========
	
	/**
	 * LAB2: Параллельный стрим без задержки с потокобезопасной коллекцией
	 */
	public static Map<Brand, Double> averageSizeByBrandParallelStream(
		java.util.Collection<Shoe> shoes) {
		return shoes.parallelStream()
			.collect(Collectors.groupingByConcurrent(
				Shoe::getBrand,
				Collectors.averagingInt(Shoe::getSize)
			));
	}

	/**
	 * LAB2: Параллельный стрим с задержкой и потокобезопасной коллекцией
	 */
	public static Map<Brand, Double> averageSizeByBrandParallelStreamWithDelay(
		java.util.Collection<Shoe> shoes, long delay) {
		return shoes.parallelStream()
			.collect(Collectors.groupingByConcurrent(
				shoe -> getBrand(shoe, delay),
				Collectors.averagingInt(Shoe::getSize)
			));
	}

	/**
	 * LAB2: Параллельный стрим с кастомным коллектором и потокобезопасной коллекцией
	 */
	public static Map<Brand, Double> averageSizeByBrandParallelWithCustomCollector(
		java.util.Collection<Shoe> shoes) {
		return shoes.parallelStream().collect(new AvgSizeByBrandCollectorConcurrent());
	}

	/**
	 * LAB2: Параллельный стрим с кастомным коллектором, задержкой и потокобезопасной коллекцией
	 */
	public static Map<Brand, Double> averageSizeByBrandParallelWithCustomCollectorAndDelay(
		java.util.Collection<Shoe> shoes, long delay) {
		return shoes.parallelStream().collect(new AvgSizeByBrandCollectorConcurrentWithDelay(delay));
	}

	// ========== LAB2: Кастомный Spliterator ==========
	
	/**
	 * LAB2: Параллельный стрим с собственным Spliterator
	 */
	public static Map<Brand, Double> averageSizeByBrandWithCustomSpliterator(
		List<Shoe> shoes) {
		Spliterator<Shoe> spliterator = new ShoeSpliterator(shoes, 0, shoes.size());
		return java.util.stream.StreamSupport.stream(spliterator, true)
			.collect(Collectors.groupingByConcurrent(
				Shoe::getBrand,
				Collectors.averagingInt(Shoe::getSize)
			));
	}

	/**
	 * LAB2: Параллельный стрим с собственным Spliterator и задержкой
	 */
	public static Map<Brand, Double> averageSizeByBrandWithCustomSpliteratorAndDelay(
		List<Shoe> shoes, long delay) {
		Spliterator<Shoe> spliterator = new ShoeSpliterator(shoes, 0, shoes.size());
		return java.util.stream.StreamSupport.stream(spliterator, true)
			.collect(Collectors.groupingByConcurrent(
				shoe -> getBrand(shoe, delay),
				Collectors.averagingInt(Shoe::getSize)
			));
	}

	// Собственный коллектор
	public static final class AvgSizeByBrandCollector implements Collector<Shoe, Map<Brand, long[]>, Map<Brand, Double>> {
		@Override
		public Supplier<Map<Brand, long[]>> supplier() {
			return HashMap::new;
			// return EnumMapLike::new;
		}

		@Override
		public BiConsumer<Map<Brand, long[]>, Shoe> accumulator() {
			return (acc, shoe) -> {
				long[] sc = acc.computeIfAbsent(shoe.getBrand(), k -> new long[2]);
				sc[0] += shoe.getSize();
				sc[1] += 1;
			};
		}

		@Override
		public BinaryOperator<Map<Brand, long[]>> combiner() {
			return (left, right) -> {
				for (Map.Entry<Brand, long[]> e : right.entrySet()) {
					long[] sc = left.computeIfAbsent(e.getKey(), k -> new long[2]);
					sc[0] += e.getValue()[0];
					sc[1] += e.getValue()[1];
				}
				return left;
			};
		}

		@Override
		public Function<Map<Brand, long[]>, Map<Brand, Double>> finisher() {
			return acc -> {
				Map<Brand, Double> res = new HashMap<>();
				// Map<Brand, Double> res = new EnumMapLike<>();
				for (Map.Entry<Brand, long[]> e : acc.entrySet()) {
					long sum = e.getValue()[0];
					long cnt = e.getValue()[1];
					res.put(e.getKey(), cnt == 0 ? 0.0 : (double) sum / cnt);
				}
				return res;
			};
		}

		@Override
		public java.util.Set<Characteristics> characteristics() {
			return java.util.Collections.emptySet();
		}
	}

	// ========== LAB2: Потокобезопасный коллектор ==========
	
	/**
	 * LAB2: Потокобезопасный коллектор для параллельных стримов
	 */
	public static final class AvgSizeByBrandCollectorConcurrent 
		implements Collector<Shoe, Map<Brand, long[]>, Map<Brand, Double>> {
		
		@Override
		public Supplier<Map<Brand, long[]>> supplier() {
			return ConcurrentHashMap::new;
		}

		@Override
		public BiConsumer<Map<Brand, long[]>, Shoe> accumulator() {
			return (acc, shoe) -> {
				long[] sc = acc.computeIfAbsent(shoe.getBrand(), k -> new long[2]);
				synchronized (sc) {
					sc[0] += shoe.getSize();
					sc[1] += 1;
				}
			};
		}

		@Override
		public BinaryOperator<Map<Brand, long[]>> combiner() {
			return (left, right) -> {
				for (Map.Entry<Brand, long[]> e : right.entrySet()) {
					long[] sc = left.computeIfAbsent(e.getKey(), k -> new long[2]);
					synchronized (sc) {
						sc[0] += e.getValue()[0];
						sc[1] += e.getValue()[1];
					}
				}
				return left;
			};
		}

		@Override
		public Function<Map<Brand, long[]>, Map<Brand, Double>> finisher() {
			return acc -> {
				Map<Brand, Double> res = new ConcurrentHashMap<>();
				for (Map.Entry<Brand, long[]> e : acc.entrySet()) {
					long sum = e.getValue()[0];
					long cnt = e.getValue()[1];
					res.put(e.getKey(), cnt == 0 ? 0.0 : (double) sum / cnt);
				}
				return res;
			};
		}

		@Override
		public java.util.Set<Characteristics> characteristics() {
			return java.util.EnumSet.of(
				Characteristics.CONCURRENT,
				Characteristics.UNORDERED
			);
		}
	}

	/**
	 * LAB2: Потокобезопасный коллектор с задержкой
	 */
	public static final class AvgSizeByBrandCollectorConcurrentWithDelay 
		implements Collector<Shoe, Map<Brand, long[]>, Map<Brand, Double>> {
		
		private final long delay;
		
		public AvgSizeByBrandCollectorConcurrentWithDelay(long delay) {
			this.delay = delay;
		}
		
		@Override
		public Supplier<Map<Brand, long[]>> supplier() {
			return ConcurrentHashMap::new;
		}

		@Override
		public BiConsumer<Map<Brand, long[]>, Shoe> accumulator() {
			return (acc, shoe) -> {
				Brand brand = getBrand(shoe, delay);
				long[] sc = acc.computeIfAbsent(brand, k -> new long[2]);
				synchronized (sc) {
					sc[0] += shoe.getSize();
					sc[1] += 1;
				}
			};
		}

		@Override
		public BinaryOperator<Map<Brand, long[]>> combiner() {
			return (left, right) -> {
				for (Map.Entry<Brand, long[]> e : right.entrySet()) {
					long[] sc = left.computeIfAbsent(e.getKey(), k -> new long[2]);
					synchronized (sc) {
						sc[0] += e.getValue()[0];
						sc[1] += e.getValue()[1];
					}
				}
				return left;
			};
		}

		@Override
		public Function<Map<Brand, long[]>, Map<Brand, Double>> finisher() {
			return acc -> {
				Map<Brand, Double> res = new ConcurrentHashMap<>();
				for (Map.Entry<Brand, long[]> e : acc.entrySet()) {
					long sum = e.getValue()[0];
					long cnt = e.getValue()[1];
					res.put(e.getKey(), cnt == 0 ? 0.0 : (double) sum / cnt);
				}
				return res;
			};
		}

		@Override
		public java.util.Set<Characteristics> characteristics() {
			return java.util.EnumSet.of(
				Characteristics.CONCURRENT,
				Characteristics.UNORDERED
			);
		}
	}

	// ========== LAB2: Собственный Spliterator ==========
	
	/**
	 * LAB2: Собственный Spliterator для оптимизации параллельной обработки
	 */
	/**
	 * LAB2: Собственный Spliterator для оптимизации параллельной обработки
	 * Оптимизирован для эффективного разделения на подзадачи
	 */
	public static final class ShoeSpliterator implements Spliterator<Shoe> {
		private final List<Shoe> shoes;
		private int start;
		private final int end;
		// Оптимальный порог: достаточно мал для хорошего параллелизма,
		// но достаточно велик, чтобы избежать излишнего разделения
		private static final int THRESHOLD = 1000;

		public ShoeSpliterator(List<Shoe> shoes, int start, int end) {
			this.shoes = shoes;
			this.start = start;
			this.end = end;
		}

		@Override
		public boolean tryAdvance(java.util.function.Consumer<? super Shoe> action) {
			if (start < end) {
				action.accept(shoes.get(start++));
				return true;
			}
			return false;
		}

		@Override
		public Spliterator<Shoe> trySplit() {
			int currentStart = start;
			int currentEnd = end;
			int size = currentEnd - currentStart;
			
			// Не разделяем, если размер меньше порога
			if (size < THRESHOLD) {
				return null;
			}
			
			// Разделяем пополам для балансировки нагрузки
			int mid = currentStart + size / 2;
			start = mid;
			return new ShoeSpliterator(shoes, currentStart, mid);
		}

		@Override
		public long estimateSize() {
			return end - start;
		}

		@Override
		public int characteristics() {
			// SIZED - известен точный размер
			// SUBSIZED - размеры подсплитераторов тоже известны
			// ORDERED - порядок элементов важен (хотя для агрегации не критично)
			// IMMUTABLE - список не изменяется во время итерации
			return Spliterator.SIZED | Spliterator.SUBSIZED | 
			       Spliterator.ORDERED | Spliterator.IMMUTABLE;
		}
	}

	/*
	private static final class EnumMapLike<K extends Enum<K>, V> extends HashMap<K, V> {
		private static final long serialVersionUID = 1L;
	}
	*/
}



