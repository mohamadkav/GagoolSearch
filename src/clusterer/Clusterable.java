package clusterer;

import java.util.List;
import java.util.Set;

public abstract class Clusterable {

	public abstract double distance(Clusterable c);
	public abstract Clusterable centroid(Set<Clusterable> s);
	
	public int mostSimilar(List<Clusterable> objects) {
		int mostSimIndex = 0;
		for(int i=1; i<objects.size(); i++) {
			if(this.distance(objects.get(i)) < this.distance(objects.get(mostSimIndex)))
				mostSimIndex = i;
		}
		return mostSimIndex;
	}
}
