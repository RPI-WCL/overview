package overview.util;

public class WeightedPair<A, B> {
	public float weight;
	public A first;
	public B second;
	public WeightedPair (A a, B b) { weight = 1; first = a; second = b; }
}
