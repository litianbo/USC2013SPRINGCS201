
package gui.test;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;

public class GuiTestSM implements TReceiver
{
	Transducer t;

	boolean offlineDone = false;

	public GuiTestSM(Transducer t)
	{
		this.t = t;
		t.register(this, TChannel.CUTTER);
		t.register(this, TChannel.SENSOR);
		t.register(this, TChannel.BREAKOUT);
		t.register(this, TChannel.MANUAL_BREAKOUT);
		t.register(this, TChannel.POPUP);
		t.register(this, TChannel.DRILL);
		t.register(this, TChannel.UV_LAMP);
		t.register(this, TChannel.WASHER);
		t.register(this, TChannel.OVEN);
		t.register(this, TChannel.PAINTER);
		t.register(this, TChannel.TRUCK);//added by monroe

		t.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_PRESSED)
		{
			Integer[] newArgs = new Integer[1];
			Integer deviceNumber = (Integer)args[0];
			
			if (deviceNumber == 8 || deviceNumber == 9 || deviceNumber == 10) 
			{
				// Do nothing for test
			} 
			else if ((deviceNumber % 2) == 0)
			{
				newArgs[0] = (Integer)args[0] / 2;
				t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
			}
		}
		else if(channel == TChannel.CUTTER && event == TEvent.WORKSTATION_LOAD_FINISHED){//added by monroe
			t.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_DO_ACTION, null);
		}
		else if (channel == TChannel.CUTTER && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
		{
			t.fireEvent(TChannel.CUTTER, TEvent.WORKSTATION_RELEASE_GLASS, null);
		}
		else if(channel == TChannel.BREAKOUT && event == TEvent.WORKSTATION_LOAD_FINISHED){//added by monroe
			t.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_DO_ACTION, null);
		}
		else if (channel == TChannel.BREAKOUT && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
		{
			t.fireEvent(TChannel.BREAKOUT, TEvent.WORKSTATION_RELEASE_GLASS, null);
		}
		else if(channel == TChannel.MANUAL_BREAKOUT && event == TEvent.WORKSTATION_LOAD_FINISHED){//added by monroe
			t.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_DO_ACTION, null);
		}
		else if (channel == TChannel.MANUAL_BREAKOUT && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
		{
			t.fireEvent(TChannel.MANUAL_BREAKOUT, TEvent.WORKSTATION_RELEASE_GLASS, null);
		}
		else if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_LOAD_FINISHED)
		{
			if (offlineDone)
				t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
			else
				t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, args);
		}
		else if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_UP)
		{
			Integer[] newArgs = new Integer[1];
			newArgs[0] = 0;
			t.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_DO_LOAD_GLASS, newArgs);
		}
		else if (channel == TChannel.DRILL && event == TEvent.WORKSTATION_LOAD_FINISHED)
		{
			t.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_DO_ACTION, args);
		}
		else if (channel == TChannel.DRILL && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
		{
			t.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_RELEASE_GLASS, args);
			offlineDone = true;
		}
		else if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_DOWN)
		{
			t.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);
			// offlineDone = false;
		}
		else if(channel == TChannel.WASHER && event == TEvent.WORKSTATION_LOAD_FINISHED){//added by monroe
			//t.fireEvent(TChannel.WASHER, TEvent.WORKSTATION_DO_ACTION, null);
		}
		else if (channel == TChannel.WASHER && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
		{
			//t.fireEvent(TChannel.WASHER, TEvent.WORKSTATION_RELEASE_GLASS, null);
		}
		else if(channel == TChannel.UV_LAMP && event == TEvent.WORKSTATION_LOAD_FINISHED){//added by monroe
			t.fireEvent(TChannel.UV_LAMP, TEvent.WORKSTATION_DO_ACTION, null);
		}
		else if (channel == TChannel.UV_LAMP && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
		{
			t.fireEvent(TChannel.UV_LAMP, TEvent.WORKSTATION_RELEASE_GLASS, null);
		}
		else if(channel == TChannel.PAINTER && event == TEvent.WORKSTATION_LOAD_FINISHED){//added by monroe
			//t.fireEvent(TChannel.PAINTER, TEvent.WORKSTATION_DO_ACTION, null);
		}
		else if (channel == TChannel.PAINTER && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
		{
			//t.fireEvent(TChannel.PAINTER, TEvent.WORKSTATION_RELEASE_GLASS, null);
		}
		else if(channel == TChannel.OVEN && event == TEvent.WORKSTATION_LOAD_FINISHED){//added by monroe
			t.fireEvent(TChannel.OVEN, TEvent.WORKSTATION_DO_ACTION, null);
		}
		else if (channel == TChannel.OVEN && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
		{
			t.fireEvent(TChannel.OVEN, TEvent.WORKSTATION_RELEASE_GLASS, null);
		}
		else if(channel==TChannel.TRUCK && event == TEvent.TRUCK_GUI_LOAD_FINISHED){//added by monroe
			t.fireEvent(TChannel.TRUCK, TEvent.TRUCK_DO_EMPTY, null);
		}
	}
}
