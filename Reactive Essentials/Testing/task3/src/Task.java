import java.time.Duration;
import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

public class Task {

	public static void unitTestAFunction(Function<Flux<String>, Flux<Long>> functionToTest) {
		testSuccessCase(functionToTest);
		testFailureCase(functionToTest);
	}

	static void testSuccessCase(Function<Flux<String>, Flux<Long>> functionToTest) {
		// produce "1" "2" "100"
		TestPublisher<String> testPublisher = TestPublisher.<String>create();
		Flux<Long> resultToTest = functionToTest.apply(testPublisher.flux());
		StepVerifier.create(resultToTest)
				.expectSubscription()
				.then(() -> testPublisher.next("1", "2", "100"))
				.expectNext(1L, 2L, 100L)
				.then(testPublisher::complete)
				.expectComplete()
				.verify(Duration.ofMillis(1000));
	}

	static void testFailureCase(Function<Flux<String>, Flux<Long>> functionToTest) {
		// produce non number string and check NumberFormatException is produced
		TestPublisher<String> testPublisher = TestPublisher.<String>create();
		Flux<Long> resultToTest = functionToTest.apply(testPublisher.flux());
		StepVerifier.create(resultToTest)
				.expectSubscription()
				.then(() -> testPublisher.next("asdas"))
				.expectError(NumberFormatException.class)
				.verify(Duration.ofMillis(1000));
	}
}