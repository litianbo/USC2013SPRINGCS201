package engine.agent.families.cf7;

import transducer.TChannel;
import transducer.Transducer;
import engine.agent.families.ConveyorFamilyInterface;
import engine.agent.families.Glass;
import engine.agent.families.cf7.SensorAgent.SensorType;

// Francesca
public class ConveyorFamilySeven implements ConveyorFamilyInterface {
	
	ConveyorFamilyInterface conveyor6, conveyor8;
	ConveyorAgent conveyor7;
	PopupAgent popup;
	SensorAgent frontSensor, backSensor;
	MachineAgent upperMachine, lowerMachine;
	public enum MachineType { DRILL, CROSS_SEAMER, GRINDER };
	final MachineType type = MachineType.GRINDER;
	
	// Turn on debug messages
	final boolean debugMessages = true;
	
	public ConveyorFamilySeven(Transducer transducer){
		
		upperMachine = new MachineAgent(transducer, 0);
		lowerMachine = new MachineAgent(transducer, 1);
		popup = new PopupAgent(transducer, 2);
		frontSensor = new SensorAgent(transducer, 14);
		backSensor = new SensorAgent(transducer, 15);
		conveyor7 = new ConveyorAgent(transducer, popup, frontSensor, backSensor, 7);

		popup.setConveyor(conveyor7);
		popup.setMachine(upperMachine, 0);
		popup.setMachine(lowerMachine, 1);
		popup.setBackSensor(backSensor);
		
		frontSensor.setConveyor(conveyor7);
		frontSensor.setPopup(null);
		frontSensor.type = SensorType.FRONT;
		
		backSensor.setConveyor(conveyor7);
		backSensor.setPopup(popup);
		backSensor.type = SensorType.BACK;
		
		upperMachine.setPopup(popup);
		lowerMachine.setPopup(popup);
		
		frontSensor.setDebugMessages(debugMessages);
		backSensor.setDebugMessages(debugMessages);
		conveyor7.setDebugMessages(debugMessages);
		upperMachine.setDebugMessages(debugMessages);
		lowerMachine.setDebugMessages(debugMessages);
		popup.setDebugMessages(debugMessages);
		
		transducer.register(frontSensor, TChannel.SENSOR);
		transducer.register(backSensor, TChannel.SENSOR);
		transducer.register(popup, TChannel.POPUP);
		transducer.register(popup, TChannel.GRINDER);
		transducer.register(conveyor7, TChannel.CONVEYOR);
		transducer.register(upperMachine, TChannel.GRINDER);
		transducer.register(lowerMachine, TChannel.GRINDER);

		upperMachine.startThread();
		lowerMachine.startThread();
		popup.startThread();
		frontSensor.startThread();
		backSensor.startThread();
		conveyor7.startThread();
		
	}
	
	public void msgHereIsGlass(Glass glass) {
		if(debugMessages){
			if(glass.getRecipe().getNeedGrinding()){
				System.out.println("ConveyorFamily7 has received glass to be processed.");
			}
			else {
				System.out.println("ConveyorFamily7 has received glass to pass on.");
			}
		}
		frontSensor.msgHereIsGlass(glass);
	}

	public void msgStartConveyor() {
		conveyor7.msgStartConveyor();
		popup.msgConveyorStarted();
	}

	public void msgStopConveyor() {
		conveyor7.msgStopConveyor();
		popup.msgConveyorStopped();
	}
	
	@Override
	public void setNextFamily(ConveyorFamilyInterface nextFamily) {
		conveyor8 = nextFamily;
		popup.setNextFamily(conveyor8);
	}

	@Override
	public void setPreviousFamily(ConveyorFamilyInterface previousFamily) {
		conveyor6 = previousFamily;
		conveyor7.setPreviousFamily(conveyor6);
	}
}
