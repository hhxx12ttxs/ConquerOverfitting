// Dot product with all vectors, returning the maximum
double max = 0;
Direction resolvedDirection = Direction.LEFT;
for (Direction direction : candidates)
{
double dotProduct = vector.x * direction.vector.x + vector.y * direction.vector.y + vector.z * direction.vector.z;

