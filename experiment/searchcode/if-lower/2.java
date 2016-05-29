String irreg = irregular.get(lowerText);
if (irreg != null) {
return (lowerText = irreg);
}
// Handle words that can be plural or singular
if (singAndPlur.contains(lowerText)) {

