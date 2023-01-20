import java.time.Duration;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class CryptoApplicationTests {

	@Test
	public void verifyIncomingMessageValidation() {
		StepVerifier.create(
				ApplicationRunner.handleRequestedAveragePriceIntervalValue(
						Flux.just("Invalid", "32", "", "-1", "1", "0", "5", "62", "5.6", "12")
				).take(Duration.ofSeconds(1)))
		            .expectNext(32L, 1L, 5L, 12L)
		            .verifyComplete();
	}

	@Test
	public void verifyOutgoingStreamBackpressure() {
		DirectProcessor<String> processor = DirectProcessor.create();

		StepVerifier
				.create(
						ApplicationRunner.handleOutgoingStreamBackpressure(processor),
						0
				)
				.expectSubscription()
				.then(() -> processor.onNext("A"))
				.then(() -> processor.onNext("B"))
				.then(() -> processor.onNext("C"))
				.then(() -> processor.onNext("D"))
				.then(() -> processor.onNext("E"))
				.then(() -> processor.onNext("F"))
				.expectNoEvent(Duration.ofMillis(300))
				.thenRequest(6)
				.expectNext("A", "B", "C", "D", "E", "F")
				.thenCancel()
				.verify();
	}
}