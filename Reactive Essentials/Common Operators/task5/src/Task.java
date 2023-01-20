import reactor.core.publisher.Flux;

public class Task {

	public static Flux<Long> transformSequence(Flux<String> input) {
		return input.transform(Task::validate)
				.transform(Task::doBusinessLogic);
	}

	private static Flux<Long> doBusinessLogic(Flux<String> flux) {
		return flux.map(s -> s.replace("0x", ""))
		           .map(s -> Long.parseLong(s, 16));
	}

	private static Flux<String> validate(Flux<String> flux) {
		return flux.filter(s -> s.length() > 0)
		           .filter(s -> s.startsWith("0x"));
	}
}