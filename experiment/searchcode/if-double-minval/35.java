output = Integer.parseInt(inputString);
} catch (Exception e) {
output = defaultValue;
}

if (output < minVal)
output = minVal;
if (maxVal == -1) {
if (output < minVal)
output = minVal;
} else if (output > maxVal)

