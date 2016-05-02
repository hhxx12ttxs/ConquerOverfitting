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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.janusproject.kernel.address.AgentAddress;
import org.janusproject.kernel.logger.LoggerUtil;

/**
 * @author $Author: sgalland$
 * @author $Author: ngaud$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class AbstractCollectiveProbeTest extends TestCase {

	private static final String A1 = "A1"; //$NON-NLS-1$
	private static final String A2 = "A2"; //$NON-NLS-1$
	
	private AbstractCollectiveProbe probe;
	private AgentAddress adr;
	
	/**
	 * @throws Exception
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();
		LoggerUtil.setGlobalLevel(Level.OFF);
		this.probe = new AbstractCollectiveProbeStub();
		this.adr = new AgentAddressStub();
	}
	
	/**
	 * @throws Exception
	 */
	@Override
	public void tearDown() throws Exception {
		this.probe = null;
		this.adr = null;
		super.tearDown();
	}	

	/**
	 * @throws Exception
	 */
	public void testGetProbeIntString() throws Exception {
		Map<AgentAddress, Integer> map = this.probe.getProbeInt(A1);
		assertNotNull(map);
		assertEquals(1, map.size());
		assertTrue(map.containsKey(this.adr));
		assertEquals(Integer.valueOf(3), map.get(this.adr));
		
		assertNull(this.probe.getProbeInt(A2));
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeLongString() throws Exception {
		Map<AgentAddress, Long> map = this.probe.getProbeLong(A1);
		assertNotNull(map);
		assertEquals(1, map.size());
		assertTrue(map.containsKey(this.adr));
		assertEquals(Long.valueOf(3), map.get(this.adr));
		
		assertNull(this.probe.getProbeLong(A2));
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeFloatString() throws Exception {
		Map<AgentAddress, Float> map = this.probe.getProbeFloat(A1);
		assertNotNull(map);
		assertEquals(1, map.size());
		assertTrue(map.containsKey(this.adr));
		assertEquals(Float.valueOf((float)Math.PI), map.get(this.adr));
		
		assertNull(this.probe.getProbeFloat(A2));
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeDoubleString() throws Exception {
		Map<AgentAddress, Double> map = this.probe.getProbeDouble(A1);
		assertNotNull(map);
		assertEquals(1, map.size());
		assertTrue(map.containsKey(this.adr));
		assertEquals(Double.valueOf(Math.PI), map.get(this.adr));
		
		assertNull(this.probe.getProbeDouble(A2));
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeBooleanString() throws Exception {
		Map<AgentAddress, Boolean> map = this.probe.getProbeBool(A1);
		assertNotNull(map);
		assertEquals(1, map.size());
		assertTrue(map.containsKey(this.adr));
		assertEquals(Boolean.TRUE, map.get(this.adr));
		
		assertNull(this.probe.getProbeBool(A2));
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeCharString() throws Exception {
		Map<AgentAddress, Character> map = this.probe.getProbeChar(A1);
		assertNotNull(map);
		assertEquals(1, map.size());
		assertTrue(map.containsKey(this.adr));
		assertEquals(Character.valueOf('3'), map.get(this.adr));
		
		assertNull(this.probe.getProbeChar(A2));
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeByteString() throws Exception {
		Map<AgentAddress, Byte> map = this.probe.getProbeByte(A1);
		assertNotNull(map);
		assertEquals(1, map.size());
		assertTrue(map.containsKey(this.adr));
		assertEquals(Byte.valueOf((byte)3), map.get(this.adr));
		
		assertNull(this.probe.getProbeByte(A2));
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeShortString() throws Exception {
		Map<AgentAddress, Short> map = this.probe.getProbeShort(A1);
		assertNotNull(map);
		assertEquals(1, map.size());
		assertTrue(map.containsKey(this.adr));
		assertEquals(Short.valueOf((short)3), map.get(this.adr));
		
		assertNull(this.probe.getProbeShort(A2));
	}

	/**
	 * @throws Exception
	 */
	public void testGetProbeStringString() throws Exception {
		Map<AgentAddress, String> map = this.probe.getProbeString(A1);
		assertNotNull(map);
		assertEquals(1, map.size());
		assertTrue(map.containsKey(this.adr));
		assertEquals(Double.toString(Math.PI), map.get(this.adr));
		
		assertNull(this.probe.getProbeString(A2));
	}

	/** 
	 * @author $Author: sgalland$
	 * @author $Author: ngaud$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class AbstractCollectiveProbeStub extends AbstractCollectiveProbe {

		/**
		 * @param adr
		 */
		public AbstractCollectiveProbeStub(AgentAddress... adr) {
			super(adr);
		}

		@Override
		public <T> Map<AgentAddress, T[]> getProbeArray(String probeName, Class<T> clazz) {
			return null;
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public <T> Map<AgentAddress, T> getProbeValue(String probeName, Class<T> clazz) {
			if (A1.equals(probeName)) {
				T value = null;
				if (Integer.class.isAssignableFrom(clazz))
					value = clazz.cast(Integer.valueOf(3));
				else if (Long.class.isAssignableFrom(clazz))
					value = clazz.cast(Long.valueOf(3));
				else if (Short.class.isAssignableFrom(clazz))
					value = clazz.cast(Short.valueOf((short)3));
				else if (Byte.class.isAssignableFrom(clazz))
					value = clazz.cast(Byte.valueOf((byte)3));
				else if (Character.class.isAssignableFrom(clazz))
					value = clazz.cast(Character.valueOf('3'));
				else if (Float.class.isAssignableFrom(clazz))
					value = clazz.cast(Float.valueOf((float)Math.PI));
				else if (Double.class.isAssignableFrom(clazz))
					value = clazz.cast(Double.valueOf(Math.PI));
				else if (Number.class.isAssignableFrom(clazz))
					value = clazz.cast(Double.valueOf(Math.PI));
				else if (Boolean.class.isAssignableFrom(clazz))
					value = clazz.cast(Boolean.TRUE);
				else if (String.class.isAssignableFrom(clazz))
					value = clazz.cast(Double.toString(Math.PI));
				
				if (value!=null) {
					Map<AgentAddress, T> map = new HashMap<AgentAddress,T>();
					map.put(AbstractCollectiveProbeTest.this.adr, value);
					return map;
				}
			}
			return null;
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public Map<AgentAddress,Object> getProbeValue(String probeName) {
			if (A1.equals(probeName)) {
				Object value = Integer.valueOf(3);
				Map<AgentAddress,Object> map = new HashMap<AgentAddress,Object>();
				map.put(AbstractCollectiveProbeTest.this.adr, value);
				return map;
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
