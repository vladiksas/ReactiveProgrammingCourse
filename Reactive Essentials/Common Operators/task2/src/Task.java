import reactor.core.publisher.Flux;

public class Task {

	public static Flux<String> transformSequence(Flux<String> flux) {
		return flux.filter(s -> s.length() > 3);
	}
}