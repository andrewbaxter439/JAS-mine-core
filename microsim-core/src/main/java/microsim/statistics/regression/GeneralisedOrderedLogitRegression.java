package microsim.statistics.regression;

import java.util.*;

import microsim.data.MultiKeyCoefficientMap;
import microsim.statistics.IDoubleSource;
import static microsim.statistics.regression.RegressionUtils.populateMultinomialCoefficientMap;

public class GeneralisedOrderedLogitRegression<T extends Enum<T> & IntegerValuedEnum> {

	private MultiChoiceMap<T> mapObject;


	public GeneralisedOrderedLogitRegression(Class<T> enumType, MultiKeyCoefficientMap multinomialCoefficients) {
		Map<T, MultiKeyCoefficientMap> maps = populateMultinomialCoefficientMap(enumType, multinomialCoefficients);
		mapObject = new MultiChoiceMap<>(enumType, maps);
		mapObject.setEnumList();
	}

	public Class<T> getEnumType() { return mapObject.getEnumType(); }

	public <E extends Enum<E>> Map<T,Double> getProbabilities(IDoubleSource iDblSrc, Class<E> Regressors) {

		Map<T, Double> scores = mapObject.getScores(iDblSrc, Regressors);
		Map<T, Double> probs = new HashMap<>();

		List<T> eventsList = mapObject.getEnumList();
		double probHere, probPreceding = 0.0;
		for (int ii = 0; ii < eventsList.size()-1; ii++) {

			T event = eventsList.get(ii);
			double expScore = Math.exp(scores.get(event));
			probHere = 1.0 / (1.0 + expScore);
			if (probHere > probPreceding) {
				double prob = probHere - probPreceding;
				probs.put(event, prob);
				probPreceding = probHere;
			} else {
				probs.put(event, -1.0);
			}
		}
		probs.put((T) eventsList.get(eventsList.size()-1), 1.0 - probPreceding);

		return probs;
	}
}
