public ProfileFollower(MotionProfile profile, double kv, double ka, MotionController correction){
m_profile = profile;
m_correction = correction;
double acceleration = m_ka*m_profile.getAcceleration(m_timer.get());

double correction = 0;
if(!m_correction.equals(null))

