public static String sql1 = &quot;update jpt_sub_series set ref_idx = ?, rec_status = &#39;DEL&#39;, update_date = current_timestamp where rec_status = &#39;ACT&#39; and material_id in (&quot;
+ &quot;select sub_series_id from jpw_application where tran_action = &#39;DEL&#39; and ref_idx = ?)&quot;;

public static String sql2 = &quot;insert into jpt_log (ref_no, severity, category, log_message, remarks_1, create_date, update_date) &quot;

