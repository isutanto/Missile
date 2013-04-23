package controller;


import model.GameWorldModel;
import view.GameView;
import view.JFrameView;

public class GameController extends AbstractController{
	public GameController(){
		setModel(new GameWorldModel());
		setView(new GameView((GameWorldModel)getModel(), this));
		((JFrameView)getView()).setVisible(true);
	}
	public void operation(String option){
		
	if(option == "pursuit")
		((GameWorldModel)getModel()).getMissile().setGuidance("pursuit");
	else if(option == "parallel")
		((GameWorldModel)getModel()).getMissile().setGuidance("parallel");
	else if (option == "proportional")
		((GameWorldModel)getModel()).getMissile().setGuidance("proportional");

	}
	public void operation(boolean paused) {
		if(paused == false)
			((GameView)getView()).pauseGame();
		else
			((GameView)getView()).resumeGame();

	}
	public void operation(char dir, boolean option) {
		throw new UnsupportedOperationException();
	}
	
}

