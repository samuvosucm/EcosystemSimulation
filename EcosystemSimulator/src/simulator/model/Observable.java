package simulator.model;

public interface Observable<T> {
	
	void add_observer(T o);
	void remove_observer(T o);
}
