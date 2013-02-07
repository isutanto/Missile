package view;
import controller.Controller;
import model.Model;
import model.*;
import controller.*;

public interface View {
	Controller getController();
	void setController(Controller controller);
	Model getModel();
	void setModel(Model model);
}
