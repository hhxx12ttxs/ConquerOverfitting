System.out.println(&quot;Error getting data: &quot; + ex);
}
finally {
if(cn != null) {
cn.close();
}
}
}

public static void saveStock(Stock s) throws SQLException {
st.setDouble(6,s.getTodaysOpen());
st.setDouble(7,s.getPreviousClose());
int rows = st.executeUpdate();
if(rows > 0) {

