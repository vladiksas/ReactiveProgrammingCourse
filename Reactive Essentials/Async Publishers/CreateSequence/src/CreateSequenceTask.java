import reactor.core.publisher.Flux;
import java.util.ArrayList;
import java.util.List;

public class CreateSequenceTask {

	public static Flux<Integer> createSequence() {
		return Flux.range(0, 20);
	}
}