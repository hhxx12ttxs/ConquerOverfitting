/**
 * Copyright (c) 2008, Mu Dynamics.
 *  All rights reserved.
 *  
 *   Redistribution and use in source and binary forms, with or without modification, 
 *   are permitted provided that the following conditions are met:
 *   
 *  - Redistributions of source code must retain the above copyright notice, 
 *     this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, 
 *     this list of conditions and the following disclaimer in the documentation and/or 
 *     other materials provided with the distribution.
 *  - Neither the name of the "Mu Dynamics" nor the names of its contributors may be used 
 *     to endorse  or promote products derived from this software without specific prior 
 *     written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.mu.jacob.core.renderer;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.mu.jacob.core.generator.GeneratorException;

/**
 * Base renderer class 
 * - manages context and paths
 * - initializes renderer
 * 
 * @author Adam Smyczek
 */
public abstract class AbstractRenderer implements IRenderer {

	/* Context map */
	private final Map<String, Object> contextMap = new HashMap<String, Object>();
	
	/* template root file */
	private File templateRoot = new File(".");
	
	/**
	 * Initializes renderer before rendering process
	 */
	protected abstract void initEngine();
	
	/**
	 * Initialize renderer, set template root
	 */
	public void initRenderer(File templateRoot) {
		if (templateRoot == null || !templateRoot.exists()) {
			throw new GeneratorException("Template root path does not exist!");
		}
		this.templateRoot = templateRoot;
		intiDefaultContext();
	}
	
	/**
	 *  Initialize engine
	 */
	private void intiDefaultContext() {
		addContext(CONTEXT_TOOLS, ContextTools.getInstance());
		addContext(CONTEXT_DATE, dateFormat.format(new Date()));
	}
	
	/**
	 * Add renderer context
	 * @param name
	 * @param ctx
	 */
	public final IRenderer addContext(final String name, final Object ctx) {
		contextMap.put(name, ctx);
		return this;
	}
	
	/**
	 * @return the contextMap
	 */
	public Map<String, Object> getContextMap() {
		return Collections.unmodifiableMap(contextMap);
	}
	
	/**
	 * @return template root
	 */
	protected File getTemplateRoot() {
		return templateRoot;
	}

	private final static DateFormat dateFormat = new SimpleDateFormat();
	
}

