import reactor.core.publisher.Flux;

public class Task {

	public static Flux<String> transformSequence(Flux<String> stringFlux) {
		return stringFlux.takeLast(2);
	}
}