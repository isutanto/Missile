package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.*;

public abstract class AbstractModel implements Model {
	@SuppressWarnings("rawtypes")
	private ArrayList listeners = new ArrayList(5);
	
	@SuppressWarnings("rawtypes")
	public void notifyChanged(ModelEvent event){
		ArrayList list = (ArrayList)listeners.clone();
		Iterator it = list.iterator();
		while(it.hasNext()){
			ModelListener ml = (ModelListener)it.next();
			ml.modelChanged(event);
		}
	}
	@SuppressWarnings("unchecked")
	public void addModelListener(ModelListener l){
		listeners.add(l);
	}
	public void removeModelListener(ModelListener l){
		listeners.remove(l);
	}
}
