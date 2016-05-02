package solar.commonresources;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;

import solar.commonresources.ErrorLog;

/**
 * @author Michael
 * @ File input class for compiler.
 */


class ReadFile {
	
	/**
	 * New Settings method obtains settings from inputlocation, returns settings object.
	 *
	 * @param Settings file name as a string.
	 * @return Settings object.
	 */
	static void NewSettings (String inputlocation)  //Settings taken from 'input location'
	//throws java.io.FileNotFoundException
	{
		//byte assemblyIndex = 0;
		//short saveto = 0;
		short fileIndex = 0;
		short cacheSpace = 50;  //Array space allocated at start of method.
		boolean indexIsValid = true;  //Tracks if the file continues.
		String line [] = new String [cacheSpace];
		line [0] = " ";

		try
		{ //If settings file exists, set Settings object to that.
			BufferedReader fr = new BufferedReader (new FileReader (inputlocation + ".conf"));

			do
			{
				ErrorLog.debug("NewSettings reader started.");
				
				while ((fileIndex < cacheSpace) && (indexIsValid))  //Write 50 lines to line.
				{
					ErrorLog.debug("Loop entered.");
					line[fileIndex] = fr.readLine();
					indexIsValid = (line[fileIndex] != null);
					fileIndex++;
					ErrorLog.debug(Short.toString(fileIndex));
				}
				

				ErrorLog.debug ("fileIndex < cacheSpace && indexIsValid loop terminated.");
				
				if(indexIsValid)  //If file continues past 50 lines, expand line using temp as a buffer.
				{
					cacheSpace += 50;
					String temp [] = new String [cacheSpace];
					System.arraycopy (line, 0, temp, 0, cacheSpace - 50);  //Assumes fileIndex is equal to cache size before that was increased.
					line = temp;
					ErrorLog.debug ("Cache:" + cacheSpace);
				}
				else
				{
					fileIndex--;
				}
			}
			while (indexIsValid);
			
			ErrorLog.debug("do while indexIsValid loop terminated.");
			
			//Settings text is in line[], now parse it into current.
			ParseSettings(line, (short)(fileIndex-1));
			
			ErrorLog.debug("Settings eventually parsed.");
			
			ErrorLog.setSettingsFound();

			fr.close();  //Throws IOException instead of FnF

		}
		catch (IOException e)  //If settings file doesn't exist, create it.
		{
			// current.settingsFound = false;
			solar.commonresources.SaveFile.settings (inputlocation);
			ErrorLog.addError(e.getMessage());
		}
		
	}
	
	/**
	 * 
	 * @param newFile object to scan.
	 * @param toSave String data to parse.
	 * @param saveSize
	 * @return Boolean value showing if parse successful.
	 * 
	 * Expects saveSize to be within the size of toSave[]
	 */
	static boolean ParseSettings (String toSave[], short saveSize)
	{
		boolean parsed = false;
		boolean inTitle = false;
		boolean inData = false;
		int span = 0;
		int tagTitle = 0;  //Stores magic number shorthand for variable name.
		int arrayIndex = 0;
		int arraySize = 0;  //Gives size of current tag array, if it is an array.
		String element;		
		StringBuffer sinput = new StringBuffer (40);
		String arrayBuffer[] = new String [1];  //arrayBuffer is temporary storage for arrayed data.  Written to
		//destination array on tag end.
		
		//Yes, this method is poorly encapsulated / not modular.
		
		
		if (saveSize > 6)  //Otherwise insufficient data to generate settings.
		{
			for (int i = 0; i < saveSize; i++)  //Iterate through lines.
			{
				toSave[i].trim();  //Reduces data to sort, white space should be ignored anyway.
				span = toSave[i].length();
				
				for (int j = 0; j < span; j++)  //Iterate through characters each line, check character against state.
				{
					if (inData)
					{
						if (toSave[i].charAt(j) == ']')  //Tag end.
						{
							//Assign sinput to current array element.
							switch (tagTitle)
							{
								case 0:  //Invalid tag name.
									break;
								case 1:  //Program name.
									Settings.programName = sinput.toString();
									break;
								case 2:  //Compile target.
									Settings.target = sinput.toString();
									break;
								case 3:  //Programming language.
									Settings.language = sinput.toString();
									break;
								case 4:  //Memory slots accessible to compiled programs.
									Settings.rAMallowed = Short.parseShort (sinput.toString());  //Need new short parser.
									break;
								case 5:  //Dump state
									Settings.dumpState = Boolean.getBoolean (sinput.toString());  //Need new bool parser.
									break;
								case 6:  //Track time
									Settings.trackTime = Boolean.getBoolean (sinput.toString());  //Need new bool parser.
									break;
								case 7:  //Arrayed data, 

									if (arraySize > arrayIndex)
									{
										arrayBuffer[arrayIndex] = sinput.toString();  //Assign data to array.

										//Assign array to newFile
										Settings.assembly = new String [arrayIndex + 1];
										System.arraycopy (arrayBuffer, 0, Settings.assembly, 0, arrayIndex);
									}
									else
									{
										/*
										//Expand array by 1 (since this can only be the last element)
										String temp [] = new String [arraySize + 1];
										for (int k = 0; k < arraySize; k++)
										{
											temp [k] = arrayBuffer [k];
										}
										arrayBuffer = temp;
										arraySize = arrayBuffer.length;  //Prevents array out of bounds error.  If size wasn't doubled, old data may be overwritten.


										arrayBuffer[arrayIndex] = sinput.toString();
										//Write data.

										(Buffer doesn't actually need to be expanded, since the buffer plus the remaining element may be saved directly)

										 */
										
										//Assume 7 is Assembly, not just any array.
										Settings.assembly = new String[arraySize + 1];  //Copy over elements found, not empty slots.
										System.arraycopy (arrayBuffer, 0, Settings.assembly, 0, arraySize);  //Don't copy the last element, it didn't fit.
										Settings.assembly [arrayIndex] = sinput.toString();  //Copy over value that doesn't fit the buffer.
									}
									break;
								default:
							}
							
							//End data.
							inData = false;
							tagTitle = 0;  //Title no longer current.
							arrayIndex = 0;  //Arrays end at tag end.
							sinput.delete(0,sinput.capacity()-1);
							//Clear sinput.
						}
						else if (toSave[i].charAt(j) == ',' && tagTitle == 7)  //Delimits array elements.
						{				//Assumes Assembly list is the only arrayed setting.
										//Only interprets commas as element separators for arrayed data.
							//Assign sinput to array.						
							if (arraySize > arrayIndex)  
							{
								arrayBuffer [arrayIndex] = sinput.toString();
							}
							else
							{
								//Expand array.
								String temp [] = new String [arraySize << 1];  //Would make temp too small (or negative size) if arraySize does not fit in 30 bits.
								//Program will crash here if temp gets a negative size.
								System.arraycopy(arrayBuffer, 0, temp, 0, arraySize);
								
								arrayBuffer = temp;
								arraySize = arrayBuffer.length;  //Prevents array out of bounds error.  If size wasn't doubled, old data may be overwritten.
							}
							arrayIndex++;  //Move to next array element.
							
							sinput.delete(0, sinput.capacity()-1);
							//Clear sinput before new data.
						}
						else
						{
							sinput.append(toSave[i].charAt(j)); //Continue through data.
							//Build data string, in the same manner as names are built.
						}
					}
					else if (inTitle)
					{
						if (toSave[i].charAt(j) == ':')  //Title:Data
						{
							//End name.
							//Determine the name of the variable to assign.
							element = sinput.toString();
							
							if (element.equalsIgnoreCase("ProgramName"))
							{
								tagTitle = 1;
							}
							else if (element.equalsIgnoreCase("Target"))
							{
								tagTitle = 2;
							}
							else if (element.equalsIgnoreCase("Language"))
							{
								tagTitle = 3;
							}
							else if (element.equalsIgnoreCase("RAMavailable"))
							{
								tagTitle = 4;
							}
							else if (element.equalsIgnoreCase("DumpState"))
							{
								tagTitle = 5;
							}
							else if (element.equalsIgnoreCase("TrackTime"))
							{
								tagTitle = 6;
							}
							else if (element.equalsIgnoreCase("Assembly"))
							{
								tagTitle = 7;
								arraySize = Settings.assembly.length;
								arrayBuffer = new String [arraySize];  //Initialise temporary Assembly storage.
							}
							
							inTitle = false;
							inData = true;
							sinput.delete(0, sinput.capacity()-1);
							//Clear sinput before data.
						}
						else if (toSave[i].charAt(j) == ']')  //Early tag [end]
						{
							inTitle = false;
							//Block ended with no distinct tag and data, ignore block.
							sinput.delete(0, sinput.capacity()-1);
							//Clear sinput.
						}
						else
						{
							sinput.append(toSave[i].charAt(j));//Continue through tag name.
							//Build name string, in the same manner as names are built.
						}
					}
					else if (toSave[i].charAt(j) == '[')  //Tag start.
					{
						inTitle = true;  //Open tag.
					}
					//None of the above, ignore non-tagged data.
				}
			}
		}
		
		return (parsed);  //Currently always returns false.
	}
	
	
	/**
	 *  Generic text file reader.
	 * 
	 */
	static SourceCode Source (String inputName, String fileExtension)
	{
		
		short fileIndex = 0;
		short cacheSpace = 50;  //Array space allocated at start of method.
		boolean indexIsValid = true;  //Tracks if the file continues.
		String line [] = new String [cacheSpace];
		SourceCode input;
		line [0] = " ";

		try
		{
			BufferedReader fr = new BufferedReader (new FileReader (inputName +"."+ fileExtension));

			do
			{
				ErrorLog.debug("Source Code reader started.");
				
				while ((fileIndex < cacheSpace) && (indexIsValid))  //Write 50 lines to line.
				{
					ErrorLog.debug("Loop entered.");
					line[fileIndex] = fr.readLine();
					indexIsValid = (line[fileIndex] != null);
					ErrorLog.debug(Short.toString(fileIndex) +" "+ line[fileIndex]);
					fileIndex++;
				}

				ErrorLog.debug ("fileIndex < cacheSpace && indexIsValid loop terminated.");
				
				if(indexIsValid)  //If file continues past 50 lines, expand line using temp as a buffer.
				{
					cacheSpace += 50;
					String temp [] = new String [cacheSpace];
					System.arraycopy (line, 0, temp, 0, cacheSpace - 50);  //Assumes fileIndex is equal to cache size before that was increased.
					line = temp;
					ErrorLog.debug (Short.toString(cacheSpace));
				}
				else
				{
					fileIndex--;  //Reader is still generating source code objects with 1 string too many.  Was 2 strings too many before this decrement.
				}
			}
			while (indexIsValid);
			fileIndex--; //Stopgap, until I understand the above problem better.
			
			ErrorLog.debug("do while indexIsValid loop terminated.");

			fr.close();  //Throws IOException instead of FnF
			
			input = new SourceCode (fileIndex + 1);
			System.arraycopy (line, 0 , input.line, 0, fileIndex);
			
			ErrorLog.debug("Everything copied from input file.");
		}
			catch (IOException e)  //Input file doesn't exist.
			{
				ErrorLog.addError(e.getMessage() + "No input file found");
				input = new SourceCode (0);
			}
		
		return input;
	}
	
	
	/**
	 * Derives a RG2 program from a text file.  Commands are marked CODE: (string).
	 * Comments are marked COMMENT:  (string)
	 * @param inputName
	 * @param fileExtension
	 * @return
	 */
	static RG2ROM RCProgram (String inputName, String fileExtension)
	{

		int length;
		int commandIndex = 0;
		int commentIndex = 0;
		String sinput;
		RG2ISA command; //Temp variable.
		RG2ROM program;
		SourceCode file;
		
		
		file = Source (inputName, fileExtension);
		length = file.line.length;
		
		program = new RG2ROM (length);
		
		for (int i=0; i<length; i++)
		{
			ErrorLog.debug("File line "+i);
			ErrorLog.debug(file.line[i]);
			
			if ((file.line[i]).startsWith("CODE: "))  //Program commands are marked "CODE: [...]"
			{				
				sinput = file.line[i].substring(5);
				command = new RG2ISA (sinput);
				
				program.command[commandIndex] = command;  //Indexes into the slot corresponding to commands found,
				//not input line.
				commandIndex++;
			}
			
			/*if ((file.line[i]).startsWith("COMMENT: ")) //Comments are marked "COMMENT:"
			{ //Includes a space inside the comment string.
				sinput = file.line[i].substring(8);
				program.inlineComments[commentIndex] = sinput;
				
				program.commentIndex [commentIndex] = commandIndex;  //Associates consecutive comments with
				//the most recent command.
				commentIndex++;
			}*/
		}
		
		return program;
	}
}

