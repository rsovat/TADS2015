package controller;

import java.io.IOException;
import java.util.List;

import javax.bluetooth.BluetoothStateException;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import bluetooth.DiscoveryDevices;
import scanner.ConnectToDevice;
import scanner.ELM327;

public class ScreenMonitorController {
	private DiscoveryDevices discoveryDevices;
	private ELM327 elm327;
	
	public ScreenMonitorController() throws BluetoothStateException, IOException {
		discoveryDevices = new DiscoveryDevices();
		discoveryDevices.discovery();
	}
	
	public void listarDevicesComboBox(JComboBox cbListDevices, JLabel lblStatus) {
		cbListDevices.removeAllItems();
		for (String device : discoveryDevices.getNamesRemoteDevices()) {
			cbListDevices.addItem(device);
		}
		lblStatus.setText(discoveryDevices.getStatus());
	}
	
	public void atualizarConexao() throws BluetoothStateException, IOException {
		discoveryDevices.discovery();
	}
	
	public void btnConnectar(int index, JLabel lblStatus) throws IOException {
		ConnectToDevice connectToDevice = new ConnectToDevice(discoveryDevices);
		elm327 = connectToDevice.connectToDevice(index);
		lblStatus.setText(connectToDevice.getStatus());
	}
	public void btnRealizarLeitura(JLabel lblRpm, JLabel lblTipoCumbustivel, 
			JLabel lblPressaoMotor,JLabel lblVelocidade) throws IOException, InterruptedException {
		lblRpm.setText("RPM: " + elm327.readRpm());
		lblTipoCumbustivel.setText("Tipo Combust�vel: " + elm327.readFindFuelType());
		lblPressaoMotor.setText("Press�o do Motor: " + elm327.readFuelPressure());
		lblVelocidade.setText("Velocidade: " + elm327.readSpeed());
	}
	
}