package ru.lab1.shoes;

import java.util.HashMap; 
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

	/*
	private static final class EnumMapLike<K extends Enum<K>, V> extends HashMap<K, V> {
		private static final long serialVersionUID = 1L;
	}
	*/
}



