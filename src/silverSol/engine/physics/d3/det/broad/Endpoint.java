package silverSol.engine.physics.d3.det.broad;

public class Endpoint implements Comparable<Endpoint> {
	public int id;
	public float value;
	public boolean minimum; //minimum or maximum
	protected int index;
	
	public Endpoint() {
		this.id = this.index = -1;
	}
	
	@Override
	public String toString() {
		return "Endpoint (" + id + ") at " + value;
	}

	@Override
	public int compareTo(Endpoint e) {
		if(this.value < e.value) return -1;
		if(this.value > e.value) return 1;
		else if(this.minimum && !e.minimum) return -1;
		else if(!this.minimum && e.minimum) return 1;
		return 0;
	}
}