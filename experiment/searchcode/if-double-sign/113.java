/*
 * Copyright (C) 2010 Aday Talavera Hierro <aday.talavera@gmail.com>
 *
 * This file is part of JASEIMOV.
 *
 * JASEIMOV is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JASEIMOV is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JASEIMOV.  If not, see <http://www.gnu.org/licenses/>.
 */
package jaseimov.server.device;

import com.phidgets.EncoderPhidget;
import com.phidgets.PhidgetException;
import jaseimov.lib.devices.AbstractDevice;
import jaseimov.lib.devices.DeviceException;
import jaseimov.lib.devices.DeviceType;
import jaseimov.lib.devices.Encoder;
import java.rmi.RemoteException;

/**
 * Implements an Encoder based in Phidgets Encoder. Uses phidgets library.
 * @author Aday Talavera Hierro <aday.talavera@gmail.com>
 */
public class PhidgetEncoderDevice extends AbstractDevice implements Encoder
{
  public final static int ABSOLUTE_POSITION = 0;

  private int phidgetSerial;
  private EncoderPhidget encoder;
  private int index;
  private double cmPerTic;
  private double radPerTic;
  private int sign;


  public PhidgetEncoderDevice(String name, int serial, int index, double wheelRadius, double ticsPerTurn, double sign) throws DeviceException
  {
    super(name, DeviceType.PHIDGET_ENCODER_SENSOR);

    this.phidgetSerial = serial;
    this.index = index;

    // tics * radPerTic = radians
    radPerTic = (2 * Math.PI) / (double) ticsPerTurn;

    // tics * cmPerTic = distance
    cmPerTic = wheelRadius * radPerTic;

    if(sign >= 0)
      this.sign = 1;
    else
      this.sign = -1;

    // Connect to phidget device
    try
    {
      encoder = new EncoderPhidget();
      encoder.open(phidgetSerial);
      encoder.waitForAttachment(DeviceConstants.PHIDGET_WAIT);      
      encoder.setPosition(index, ABSOLUTE_POSITION);
    }
    catch (PhidgetException ex)
    {
      throw new DeviceException(ex.getDescription());
    }
  }

  public int getTics() throws RemoteException, DeviceException
  {
    try
    {
      int position = encoder.getPosition(index);
      encoder.setPosition(index, ABSOLUTE_POSITION);
      return position * sign;
      //return position;
    }
    catch (PhidgetException ex)
    {
      throw new DeviceException(ex.getDescription());
    }
  }


  public double getCmPerTic() throws RemoteException, DeviceException
  {    
    return cmPerTic;
  }

  public double getRadPerTic() throws RemoteException, DeviceException
  {    
    return radPerTic;
  }

  public Object update() throws RemoteException, DeviceException
  {
    return getTics();
  }

  @Override
  public void closeDevice() throws DeviceException
  {
    try
    {
      encoder.close();
    }
    catch (PhidgetException ex)
    {
      throw new DeviceException(ex.getDescription());
    }
  }
}

