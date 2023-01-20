import java.time.Duration;
import java.util.function.Supplier;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class Task {

	public static void verifyEmissionWithVirtualTimeScheduler() {
		Supplier<Flux<Long>> toVerify = () -> Flux.interval(Duration.ofDays(1))
				.take(15)
				.skip(5);
		StepVerifier.withVirtualTime(toVerify)
				.expectSubscription()
				.expectNoEvent(Duration.ofDays(5))
				.thenAwait(Duration.ofDays(1))
				.expectNext(5L)
				.thenAwait(Duration.ofDays(10))
				.expectNext(6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L)
				.expectComplete()
				.verify(Duration.ofMillis(1000));
	}
}