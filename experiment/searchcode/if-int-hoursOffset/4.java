int minutsOffset = gMTOffset % 100; 						// in the example : 1
int hoursOffset = (gMTOffset - minutsOffset) / 100; 		// in the example : 30
int offsetInSeconds = minutsOffset * 60 + hoursOffset * 3600; 	// calculate the offset in seconds

