/*******************************************************************************
 * Copyright © 2011, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.edt.compiler.internal;

/**
 * @author jshavor
 * 
 * This class represent an EGL system word which is a variable system word
 */
public class EGLSystemVariable extends EGLSystemWord {

	private int length;
	private int arrayLength;
	private String primitiveType;
	
	public EGLSystemVariable(String name, int type, int specialFunctionType) {
		this(name, type, specialFunctionType, null, 0);
	}
	
	public EGLSystemVariable(String name, int type, int specialFunctionType, String primitiveType, int length) {
		this(name, type, specialFunctionType, primitiveType, length, IEGLConstants.KEYWORD_SYSVAR.toLowerCase());	// Jeff 11-12 - All system variables in sysvar library
	}
	
	public EGLSystemVariable(String name, int type, int specialFunctionType, String primitiveType, int length, String library) {
		this( name, type, specialFunctionType, primitiveType, length, library, -1 );
	}
	
	public EGLSystemVariable(String name, int type, int specialFunctionType, int length, int arrayLength) {
		this(name, type, specialFunctionType, null, 0, arrayLength);
	}
	
	public EGLSystemVariable(String name, int type, int specialFunctionType, String primitiveType, int length, int arrayLength) {
		this(name, type, specialFunctionType, primitiveType, length, IEGLConstants.KEYWORD_SYSVAR.toLowerCase(), arrayLength);	// Jeff 11-12 - All system variables in sysvar library
	}
	
	public EGLSystemVariable(String name, int type, int specialFunctionType, String primitiveType, int length, String library, int arrayLength) {
		super(name, type, specialFunctionType, library);	// Jeff 11-12 - All system variables in sysvar library
		this.length = length;
		this.arrayLength = arrayLength;
		this.primitiveType = primitiveType;
		if (isArrayWord())
			this.additonalInformation = EGLBaseNlsStrings.CAProposalArrayVariableSystemWord;
		else
			this.additonalInformation = EGLBaseNlsStrings.CAProposalVariableSystemWord;
	}
	

	public boolean isSystemVariable() {
		return true;
	}

	public boolean isCharacterVariable() {
		return (type & EGLSystemWordHandler.SYSTEM_WORD_CHARACTER) != 0;
	}

	public boolean isNumericVariable() {
		return (type & EGLSystemWordHandler.SYSTEM_WORD_NUMERIC) != 0;
	}
	
	public boolean isReferenceTypeVariable() {
		return (type & EGLSystemWordHandler.SYSTEM_WORD_NAMEDTYPE) != 0;
		}
	
	/* (non-Javadoc)
	 * @see org.eclipse.edt.compiler.internal.EGLSystemWord#isPresentationAttributes()
	 */
	public boolean isPresentationAttributes() {
		return primitiveType != null && primitiveType.equals( IEGLConstants.EGL_CONSOLE_UI_PRESENTATIONATTRIBUTES_STRING );
	}
	
	public int getLength() {
		return length;
	}
	
	public int getArrayLength() {
		return arrayLength;	
	}
	
	public boolean isArray() {
		return arrayLength != -1;	
	}
	
	public String getPrimitiveType()
	{
		return primitiveType;
	}
	
	public boolean isReadOnly() {
		return (type & EGLSystemWordHandler.SYSTEM_WORD_READ_ONLY) != 0;
	}
}

