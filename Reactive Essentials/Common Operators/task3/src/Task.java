import reactor.core.publisher.Flux;

public class Task {

	public static Flux<Character> createSequence(Flux<String> stringFlux) {
		return stringFlux.concatMap(word -> Flux.fromArray(word.split("")))
				.map(letter -> letter.charAt(0));
	}
}