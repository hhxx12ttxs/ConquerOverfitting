/* 
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on <http://www.janus-project.org>
 * Copyright (C) 2010-2012 Janus Core Developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.janusproject.kernel.probe;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;

import org.janusproject.kernel.address.AgentAddress;
import org.janusproject.kernel.logger.LoggerUtil;
import junit.framework.TestCase;

/** 
 * @author $Author: sgalland$
 * @author $Author: ngaud$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class AbstractIndividualProbeTest extends TestCase {

	private static final String A1 = "A1"; //$NON-NLS-1$
	private static final String A2 = "A2"; //$NON-NLS-1$
	
	private AbstractIndividualProbe probe;
	
	/**
	 * @throws Exception
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();
		LoggerUtil.setGlobalLevel(Level.OFF);
		this.probe = new AbstractIndividualProbeStub(new AgentAddressStub());
	}
	
	/**
	 * @throws Exception
	 */
	@Override
	public void tearDown() throws Exception {
		this.probe = null;
		super.tearDown();
	}	
	
	/**
	 * @throws Exception
	 */
	public void testGetProbeIntString() throws Exception {
		assertEquals(3, this.probe.getProbeInt(A1));
		try {
			this.probe.getProbeInt(A2);
			fail("ProbeValueNotDefinedException was expected"); //$NON-NLS-1$
		}
		catch(ProbeValueNotDefinedException _) {
			// Expected exception
		}
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeLongString() throws Exception {
		assertEquals(3, this.probe.getProbeLong(A1));
		try {
			this.probe.getProbeLong(A2);
			fail("ProbeValueNotDefinedException was expected"); //$NON-NLS-1$
		}
		catch(ProbeValueNotDefinedException _) {
			// Expected exception
		}
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeFloatString() throws Exception {
		assertEquals((float)Math.PI, this.probe.getProbeFloat(A1));
		try {
			this.probe.getProbeFloat(A2);
			fail("ProbeValueNotDefinedException was expected"); //$NON-NLS-1$
		}
		catch(ProbeValueNotDefinedException _) {
			// Expected exception
		}
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeDoubleString() throws Exception {
		assertEquals(Math.PI, this.probe.getProbeDouble(A1));
		try {
			this.probe.getProbeDouble(A2);
			fail("ProbeValueNotDefinedException was expected"); //$NON-NLS-1$
		}
		catch(ProbeValueNotDefinedException _) {
			// Expected exception
		}
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeBoolString() throws Exception {
		assertTrue(this.probe.getProbeBool(A1));
		try {
			this.probe.getProbeInt(A2);
			fail("ProbeValueNotDefinedException was expected"); //$NON-NLS-1$
		}
		catch(ProbeValueNotDefinedException _) {
			// Expected exception
		}
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeCharString() throws Exception {
		assertEquals('3', this.probe.getProbeChar(A1));
		try {
			this.probe.getProbeChar(A2);
			fail("ProbeValueNotDefinedException was expected"); //$NON-NLS-1$
		}
		catch(ProbeValueNotDefinedException _) {
			// Expected exception
		}
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeByteString() throws Exception {
		assertEquals((byte)3, this.probe.getProbeByte(A1));
		try {
			this.probe.getProbeByte(A2);
			fail("ProbeValueNotDefinedException was expected"); //$NON-NLS-1$
		}
		catch(ProbeValueNotDefinedException _) {
			// Expected exception
		}
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeShortString() throws Exception {
		assertEquals((short)3, this.probe.getProbeShort(A1));
		try {
			this.probe.getProbeShort(A2);
			fail("ProbeValueNotDefinedException was expected"); //$NON-NLS-1$
		}
		catch(ProbeValueNotDefinedException _) {
			// Expected exception
		}
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeStringString() throws Exception {
		assertEquals(Double.toString(Math.PI), this.probe.getProbeString(A1));
		try {
			this.probe.getProbeString(A2);
			fail("ProbeValueNotDefinedException was expected"); //$NON-NLS-1$
		}
		catch(ProbeValueNotDefinedException _) {
			// Expected exception
		}
	}

	/** 
	 * @author $Author: sgalland$
	 * @author $Author: ngaud$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class AbstractIndividualProbeStub extends AbstractIndividualProbe {

		/**
		 * @param adr
		 */
		public AbstractIndividualProbeStub(AgentAddress adr) {
			super(adr);
		}

		@Override
		public <T> T[] getProbeArray(String probeName, Class<T> clazz) {
			return null;
		}

		@Override
		public <T> T getProbeValue(String probeName, Class<T> clazz) {
			if (A1.equals(probeName)) {
				if (Integer.class.isAssignableFrom(clazz))
					return clazz.cast(Integer.valueOf(3));
				if (Long.class.isAssignableFrom(clazz))
					return clazz.cast(Long.valueOf(3));
				if (Short.class.isAssignableFrom(clazz))
					return clazz.cast(Short.valueOf((short)3));
				if (Byte.class.isAssignableFrom(clazz))
					return clazz.cast(Byte.valueOf((byte)3));
				if (Character.class.isAssignableFrom(clazz))
					return clazz.cast(Character.valueOf('3'));
				if (Float.class.isAssignableFrom(clazz))
					return clazz.cast(Float.valueOf((float)Math.PI));
				if (Double.class.isAssignableFrom(clazz))
					return clazz.cast(Double.valueOf(Math.PI));
				if (Number.class.isAssignableFrom(clazz))
					return clazz.cast(Double.valueOf(Math.PI));
				if (Boolean.class.isAssignableFrom(clazz))
					return clazz.cast(Boolean.TRUE);
				if (String.class.isAssignableFrom(clazz))
					return clazz.cast(Double.toString(Math.PI));
			}
			return null;
		}

		@Override
		public Object getProbeValue(String probeName) {
			if (A1.equals(probeName)) {
				return Integer.valueOf(3);
			}
			return null;
		}

		@Override
		public Set<String> getProbedValueNames() {
			return Collections.singleton(A1);
		}

		@Override
		public boolean hasProbeValue(String probeValueName) {
			return A1.equals(probeValueName);
		}

		@Override
		public boolean hasProbeValues() {
			return true;
		}
		
	}
	
}
