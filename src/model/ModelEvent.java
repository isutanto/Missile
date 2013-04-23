package model;
import java.awt.event.ActionEvent;
import java.awt.event.*;

@SuppressWarnings("serial")
public class ModelEvent extends ActionEvent {
	private int amount;
	public ModelEvent(Object obj, int id, String message, int amount){
		super(obj, id, message);
		this.amount = amount;
	}
	public int getAmount(){return amount;}
}
