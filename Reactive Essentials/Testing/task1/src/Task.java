import java.time.Duration;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class Task {

	public static void verifyThat10ElementsEmitted(Flux<Integer> flux) {
		StepVerifier.create(flux)
				.expectSubscription()
				.expectNextCount(10)
				.expectComplete()
				.verify(Duration.ofMillis(100));
	}
}