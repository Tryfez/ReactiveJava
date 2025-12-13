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

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableConverter;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.reactivestreams.Subscription;

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

	// ========== Методы с задержкой ==========
	
	/**
	 * Получить бренд с задержкой (имитация получения из БД)
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
	 * Стандартный стрим с задержкой получения бренда
	 */
	public static Map<Brand, Double> averageSizeByBrandStreamWithDelay(
		java.util.Collection<Shoe> shoes, long delay) {
		return shoes.stream()
			.collect(Collectors.groupingBy(
				shoe -> getBrand(shoe, delay),
				Collectors.averagingInt(Shoe::getSize)
			));
	}

	// ========== Параллельные стримы ==========
	
	/**
	 * Параллельный стрим без задержки с потокобезопасной коллекцией
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
	 * Параллельный стрим с задержкой и потокобезопасной коллекцией
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
	 * Параллельный стрим с кастомным коллектором и потокобезопасной коллекцией
	 */
	public static Map<Brand, Double> averageSizeByBrandParallelWithCustomCollector(
		java.util.Collection<Shoe> shoes) {
		return shoes.parallelStream().collect(new AvgSizeByBrandCollectorConcurrent());
	}

	/**
	 * Параллельный стрим с кастомным коллектором, задержкой и потокобезопасной коллекцией
	 */
	public static Map<Brand, Double> averageSizeByBrandParallelWithCustomCollectorAndDelay(
		java.util.Collection<Shoe> shoes, long delay) {
		return shoes.parallelStream().collect(new AvgSizeByBrandCollectorConcurrentWithDelay(delay));
	}

	// ========== Кастомный Spliterator ==========
	
	/**
	 * Параллельный стрим с собственным Spliterator
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
	 * Параллельный стрим с собственным Spliterator и задержкой
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

	// ========== Потокобезопасный коллектор ==========
	
	/**
	 * Потокобезопасный коллектор для параллельных стримов
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
	 * Потокобезопасный коллектор с задержкой
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

	// ========== Собственный Spliterator ==========
	
	/**
	 * Собственный Spliterator для оптимизации параллельной обработки
	 */
	public static final class ShoeSpliterator implements Spliterator<Shoe> {
		private final List<Shoe> shoes;
		private int start;
		private final int end;
		private static final int THRESHOLD = 1000; // Порог для разделения

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
			
			if (size < THRESHOLD) {
				return null; // Не разделяем, если размер меньше порога
			}
			
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
			return Spliterator.SIZED | Spliterator.SUBSIZED | 
			       Spliterator.ORDERED | Spliterator.IMMUTABLE;
		}
	}

	// ========== Реактивные потоки RxJava ==========
	
	/**
	 * Подсчет статистики с помощью Observable и многопоточного Scheduler
	 * Использует Observable для асинхронной обработки с задержкой
	 */
	public static Map<Brand, Double> averageSizeByBrandObservable(
		java.util.Collection<Shoe> shoes, long delay) {
		return Observable.fromIterable(shoes)
			.flatMap(shoe -> 
				Observable.just(shoe)
					.subscribeOn(Schedulers.io()) // Асинхронное получение бренда с задержкой
					.map(s -> {
						Brand brand = getBrand(s, delay);
						return new ShoeBrandPair(s, brand);
					}),
				true, // delayErrors = true для лучшей производительности
				Runtime.getRuntime().availableProcessors() // Максимальная параллельность
			)
			.observeOn(Schedulers.computation()) // Многопоточная обработка результатов
			.collect(
				() -> new ConcurrentHashMap<Brand, long[]>(),
				(acc, pair) -> {
					long[] sc = acc.computeIfAbsent(pair.brand, k -> new long[2]);
					synchronized (sc) {
						sc[0] += pair.shoe.getSize();
						sc[1] += 1;
					}
				}
			)
			.map(acc -> calculateAverages(acc))
			.blockingGet();
	}
	
	/**
	 * Подсчет статистики с помощью Observable без задержки (для сравнения)
	 */
	public static Map<Brand, Double> averageSizeByBrandObservableNoDelay(
		java.util.Collection<Shoe> shoes) {
		return Observable.fromIterable(shoes)
			.subscribeOn(Schedulers.io())
			.observeOn(Schedulers.computation())
			.collect(
				() -> new ConcurrentHashMap<Brand, long[]>(),
				(acc, shoe) -> {
					Brand brand = shoe.getBrand();
					long[] sc = acc.computeIfAbsent(brand, k -> new long[2]);
					synchronized (sc) {
						sc[0] += shoe.getSize();
						sc[1] += 1;
					}
				}
			)
			.map(acc -> calculateAverages(acc))
			.blockingGet();
	}
	
	/**
	 * Подсчет статистики с помощью Flowable и собственного Subscriber с backpressure
	 * Генерация элементов производится асинхронно с поддержкой backpressure
	 */
	public static Map<Brand, Double> averageSizeByBrandFlowableWithBackpressure(
		int count, long seed) {
		// Создаем список элементов для Flowable (генерация без задержки)
		List<Shoe> shoes = Generators.generateShoes(count, seed);
		
		return Flowable.fromIterable(shoes)
			.onBackpressureBuffer() // Управление backpressure
			.observeOn(Schedulers.io())
			.flatMap(shoe -> Flowable.just(shoe), false, Runtime.getRuntime().availableProcessors())
			.observeOn(Schedulers.computation())
			.to(new ShoeFlowableConverter(128L))
			.blockingGet();
	}
	
	/**
	 * Вспомогательный класс для пары Shoe-Brand
	 */
	private static final class ShoeBrandPair {
		final Shoe shoe;
		final Brand brand;
		
		ShoeBrandPair(Shoe shoe, Brand brand) {
			this.shoe = shoe;
			this.brand = brand;
		}
	}
	
	/**
	 * FlowableConverter для подсчета статистики с регулированием скорости поступления элементов
	 * Реализует backpressure для контроля скорости обработки
	 */
	public static final class ShoeFlowableConverter 
		implements FlowableConverter<Shoe, Single<Map<Brand, Double>>>, org.reactivestreams.Subscriber<Shoe> {
		
		private Map<Brand, long[]> accumulator = new ConcurrentHashMap<>();
		private io.reactivex.rxjava3.subjects.SingleSubject<Map<Brand, Double>> single = 
			io.reactivex.rxjava3.subjects.SingleSubject.create();
		private Subscription subscription;
		private final long batchSize;
		private long processedElementsCounter = 0L;
		
		public ShoeFlowableConverter(long batchSize) {
			this.batchSize = batchSize;
		}
		
		@Override
		public void onSubscribe(Subscription s) {
			this.subscription = s;
			subscription.request(batchSize);
		}
		
		@Override
		public void onNext(Shoe shoe) {
			Brand brand = shoe.getBrand();
			long[] sc = accumulator.computeIfAbsent(brand, k -> new long[2]);
			synchronized (sc) {
				sc[0] += shoe.getSize();
				sc[1] += 1;
			}
			
			processedElementsCounter++;
			if (processedElementsCounter % batchSize == 0) {
				subscription.request(batchSize);
			}
		}
		
		@Override
		public void onError(Throwable t) {
			t.printStackTrace();
			single.onError(t);
		}
		
		@Override
		public void onComplete() {
			Map<Brand, Double> result = new HashMap<>();
			for (Map.Entry<Brand, long[]> e : accumulator.entrySet()) {
				long sum = e.getValue()[0];
				long cnt = e.getValue()[1];
				result.put(e.getKey(), cnt == 0 ? 0.0 : (double) sum / cnt);
			}
			single.onSuccess(result);
		}
		
		@Override
		public Single<Map<Brand, Double>> apply(Flowable<Shoe> upstream) {
			upstream.subscribe(this);
			return single;
		}
	}
	
	/**
	 * Вспомогательный метод для вычисления средних значений
	 */
	private static Map<Brand, Double> calculateAverages(Map<Brand, long[]> accumulator) {
		Map<Brand, Double> result = new HashMap<>();
		for (Map.Entry<Brand, long[]> e : accumulator.entrySet()) {
			long sum = e.getValue()[0];
			long cnt = e.getValue()[1];
			result.put(e.getKey(), cnt == 0 ? 0.0 : (double) sum / cnt);
		}
		return result;
	}

	/*
	private static final class EnumMapLike<K extends Enum<K>, V> extends HashMap<K, V> {
		private static final long serialVersionUID = 1L;
	}
	*/
}



