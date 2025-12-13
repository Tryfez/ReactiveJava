package ru.lab1.shoes.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import ru.lab1.shoes.Aggregations;
import ru.lab1.shoes.Generators;
import ru.lab1.shoes.model.Shoe;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ru.lab1.shoes.model.Brand;

/**
 * LAB2: JMH бенчмарки для измерения производительности различных методов агрегации
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class AggregationBenchmark {

	// Расширенный диапазон для поиска точки равенства производительности
	@Param({"100", "500", "1000", "2000", "5000", "10000", "20000", "50000", "100000", "200000"})
	public int size;

	private List<Shoe> shoes;
	private static final long SEED = 37L;
	private static final long DELAY_MS = 1; // 1 мс задержка

	@Setup
	public void setup() {
		shoes = Generators.generateShoes(size, SEED);
	}

	// ========== LAB2: Последовательные стримы ==========
	
	@Benchmark
	public Map<Brand, Double> sequentialStream() {
		return Aggregations.averageSizeByBrandStream(shoes);
	}

	@Benchmark
	public Map<Brand, Double> sequentialStreamWithDelay() {
		return Aggregations.averageSizeByBrandStreamWithDelay(shoes, DELAY_MS);
	}

	// ========== LAB2: Параллельные стримы ==========
	
	@Benchmark
	public Map<Brand, Double> parallelStream() {
		return Aggregations.averageSizeByBrandParallelStream(shoes);
	}

	@Benchmark
	public Map<Brand, Double> parallelStreamWithDelay() {
		return Aggregations.averageSizeByBrandParallelStreamWithDelay(shoes, DELAY_MS);
	}

	// ========== LAB2: Параллельные стримы с кастомным коллектором ==========
	
	@Benchmark
	public Map<Brand, Double> parallelWithCustomCollector() {
		return Aggregations.averageSizeByBrandParallelWithCustomCollector(shoes);
	}

	@Benchmark
	public Map<Brand, Double> parallelWithCustomCollectorAndDelay() {
		return Aggregations.averageSizeByBrandParallelWithCustomCollectorAndDelay(shoes, DELAY_MS);
	}

	// ========== LAB2: Параллельные стримы с собственным Spliterator ==========
	
	@Benchmark
	public Map<Brand, Double> parallelWithCustomSpliterator() {
		return Aggregations.averageSizeByBrandWithCustomSpliterator(shoes);
	}

	@Benchmark
	public Map<Brand, Double> parallelWithCustomSpliteratorAndDelay() {
		return Aggregations.averageSizeByBrandWithCustomSpliteratorAndDelay(shoes, DELAY_MS);
	}

	// Запуск бенчмарков
	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
			.include(AggregationBenchmark.class.getSimpleName())
			.build();

		new Runner(opt).run();
	}
}

