public static NguoiPhuTrachDAO getInstance() {
if (nptDAO == null) {
nptDAO = new NguoiPhuTrachDAO();
while (rs.next()) {
NguoiPhuTrach npt = new NguoiPhuTrach();
npt.setMaNPT(rs.getInt(&quot;maNPT&quot;));
npt.setHoTen(rs.getString(&quot;hoTen&quot;));

