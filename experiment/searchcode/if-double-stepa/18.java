//		double deltaY = them.getY() - me.getY();
//		double bearing;
//		if (deltaY == 0.0) {
//			bearing = ( ( deltaY < 0.0 ) ? PhysicsConstants.west : PhysicsConstants.east );
double tangent = Physics.posTangentAngle( velocityVector.getDirection() );
Point2D.Double stepA = Physics.addVector( position.getPosition( ), velocityVector.getDirection(), backupDistance );

