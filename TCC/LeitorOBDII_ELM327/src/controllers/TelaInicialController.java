package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import device.DiscoveryDevices;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class TelaInicialController implements Initializable{
	
	@FXML
	private Button btnConnect, btnRefresh, btnRpm, btnPressaoMotor, btnVelocidade;
	@FXML
	private Label lblMedicao, lblStConnect;
	@FXML
	private ComboBox cBoxDeviceSelect;
	
	private DiscoveryDevices discovery;
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		discovery = new DiscoveryDevices();
		discovery.carregarComponentes(cBoxDeviceSelect, lblMedicao, lblStConnect);
		try {
			discovery.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

}
