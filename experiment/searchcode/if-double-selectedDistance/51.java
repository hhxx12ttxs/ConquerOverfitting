private Connection conn;
private PreparedStatement pstmt;
private ResultSet rs;
private double userLatitude, userLongitude, difference;
public void setUser(userDetails user) {
this.user = user;
}

private double distance;
private double earthRadius = 3959.0;

