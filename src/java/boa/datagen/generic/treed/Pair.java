package boa.datagen.generic.treed;

/**
 * @author hoan
 *
 */
public class Pair {
	private Object obj1, obj2;
	private double weight = -1.0, weight1 = 0;

	public Pair(Object obj1, Object obj2) {
		this.obj1 = obj1;
		this.obj2 = obj2;
	}

	public Pair(Object obj1, Object obj2, double weight) {
		this(obj1, obj2);
		this.weight = weight;
	}

	public Pair(Object obj1, Object obj, double weight, double weight1) {
		this(obj1, obj, weight);
		this.weight1 = weight1;
	}

	public double computeWeight(Pair other) {
		return this.weight;
	}

	public int compareTo(Pair other) {
		int c = compare(this.weight, other.weight);
		if (c == 0)
			c = compare(this.weight1, other.weight1);
		return c;
		// return (int)(this.weight - other.getWeight());
	}

	private int compare(double d1, double d2) {
		if (d1 > d2) return 1;
		if (d1 < d2) return -1;
		return 0;
	}

	public Object getObj1() {
		return obj1;
	}

	public void setObj1(Object obj1) {
		this.obj1 = obj1;
	}

	public Object getObj2() {
		return obj2;
	}

	public void setObj2(Object obj2) {
		this.obj2 = obj2;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return String.valueOf(this.weight);
	}
}
