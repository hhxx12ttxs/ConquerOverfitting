package uk.co.HariboPenguin.uReport;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportDataHandler {

    public Report plugin;

    public ReportDataHandler(Report instance) {
        this.plugin = instance;
    }

    public boolean sendReport(String reporter, String reportedPlayer, String reason, String textDate, Integer reportID) throws SQLException {
        String query = "INSERT INTO  `reports` (`reportID` ,`reportdate` ,`reporter` ,`reportedplayer` ,`reason`) VALUES ('" + reportID + "', '" + textDate + "',  '" + reporter + "',  '" + reportedPlayer + "',  '" + reason + "')";
        plugin.dbManage.query(query);
        return true;

    }

    public boolean sendReportWithTPInfo(String reporter, String reportedPlayer, String reason, String textDate, Integer reportID, String worldName, Double xLoc, Double yLoc, Double zLoc, Float pitch, Float yaw) throws SQLException {
        String query = "INSERT INTO  `reports` (`reportID` ,`reportdate` ,`reporter` ,`reportedplayer` ,`reason` ,`worldName` ,`xLoc` ,`yLoc` ,`zLoc` ,`pitch` ,`yaw`) VALUES ('" + reportID + "', '" + textDate + "',  '" + reporter + "',  '" + reportedPlayer + "',  '" + reason + "', '" + worldName + "', '" + xLoc + "', '" + yLoc + "', '" + zLoc + "', '" + pitch + "', '" + yaw + "')";
        plugin.dbManage.query(query);
        return true;

    }

    public boolean getReports(CommandSender sender, String playername, int pageNumber) throws SQLException {

        String query = "SELECT * FROM  `reports` WHERE  `reportedplayer` LIKE '" + playername + "' ORDER BY 'reports'.'reportID' DESC";
        String getNumberOfReports = "SELECT COUNT(*) AS count FROM reports WHERE reportedplayer LIKE '" + playername + "'";
        ResultSet result = plugin.dbManage.query(query);
        ResultSet resultCount = plugin.dbManage.query(getNumberOfReports);

        int numberOfResults = resultCount.getInt("count");

        int resultsPerPage = plugin.getConfig().getInt("reports-per-page");

        int pageCount = Math.round((int) Math.ceil((numberOfResults - 1) / resultsPerPage)) + 1;

        if (pageNumber > pageCount) {
            sender.sendMessage(plugin.prefix + ChatColor.WHITE + "- " + ChatColor.RED + "That page does not exist!");
            result.close();
            resultCount.close();
            return true;
        }

        if (numberOfResults == 0) {
            sender.sendMessage(plugin.prefix + ChatColor.WHITE + "- " + ChatColor.RED + "No Reports Found!");
            result.close();
            resultCount.close();
            return true;
        }

        sender.sendMessage(ChatColor.WHITE + "---------" + ChatColor.GRAY + "[ " + ChatColor.GREEN + "uReport - Report for " + playername + ChatColor.GRAY + " ]" + ChatColor.WHITE + "---------");
        sender.sendMessage(ChatColor.WHITE + "-------------------" + ChatColor.GRAY + "[ " + ChatColor.GREEN + "Page " + pageNumber + " / " + pageCount + ChatColor.GRAY + " ]" + ChatColor.WHITE + "--------------------");

        if (result == null) {
            plugin.log.info("No results found");
            result.close();
            resultCount.close();
            return false;
        }

        if (pageNumber > 1) {
            int startRow = pageNumber * resultsPerPage - resultsPerPage;

            for (int x = 0; x < startRow; x++) {
                result.next();
            }
        }

        int counter = 1;

        while (result.next() && counter <= resultsPerPage) {
            String reportid = result.getString("reportID");
            String reportdate = result.getString("reportdate");
            String reporter = result.getString("reporter");
            // String reportedplayer = result.getString("reportedplayer");
            String reason = result.getString("reason");

            sender.sendMessage(ChatColor.GRAY + "#" + reportid + " " + reportdate + " by " + ChatColor.GREEN + reporter + ChatColor.WHITE + " - " + ChatColor.GRAY + reason);

            counter++;

            //	if (result.isLast()) break;
        }

        result.close();
        resultCount.close();

        return true;

    }

    public boolean getLatestReports(CommandSender sender, String playername, int pageNumber) throws SQLException {

        String query = "SELECT * FROM  `reports` WHERE  `reportedplayer` IS NOT NULL ORDER BY 'reports'.'reportID' DESC";
        String getNumberOfReports = "SELECT COUNT(*) AS count FROM reports WHERE reportedplayer IS NOT NULL";
        ResultSet result = plugin.dbManage.query(query);
        ResultSet resultCount = plugin.dbManage.query(getNumberOfReports);

        int numberOfResults = resultCount.getInt("count");

        int resultsPerPage = plugin.getConfig().getInt("reports-per-page");

        int pageCount = Math.round((int) Math.ceil((numberOfResults - 1) / resultsPerPage)) + 1;

        if (pageNumber > pageCount) {
            sender.sendMessage(plugin.prefix + ChatColor.WHITE + "- " + ChatColor.RED + "That page does not exist!");
            result.close();
            resultCount.close();
            return true;
        }

        if (numberOfResults == 0) {
            sender.sendMessage(plugin.prefix + ChatColor.WHITE + "- " + ChatColor.RED + "No Reports Found!");
            result.close();
            resultCount.close();
            return true;
        }

        sender.sendMessage(ChatColor.WHITE + "-------------" + ChatColor.GRAY +  "[" + ChatColor.GREEN + " uReport - Latest Reports " + ChatColor.GRAY + "]" + ChatColor.WHITE + "-------------");
        sender.sendMessage(ChatColor.WHITE + "-------------------" + ChatColor.GRAY +  "[ " + ChatColor.GREEN + "Page " + pageNumber + " / " + pageCount + ChatColor.GRAY + " ]" + ChatColor.WHITE + "--------------------");

        if (result == null) {
            plugin.log.info("No results found");
            result.close();
            resultCount.close();
            return false;
        }

        if (pageNumber > 1) {
            int startRow = pageNumber * resultsPerPage - resultsPerPage;

            for (int x = 0; x < startRow; x++) {
                result.next();
            }
        }

        int counter = 1;

        while (result.next() && counter <= resultsPerPage) {

            String reportid = result.getString("reportID");
            String reportdate = result.getString("reportdate");
            // String reporter = result.getString("reporter");
            String reportedplayer = result.getString("reportedplayer");
            String reason = result.getString("reason");

            sender.sendMessage(ChatColor.GRAY + "#" + reportid + " " + reportdate + ChatColor.WHITE + " - " + ChatColor.GREEN + reportedplayer + ChatColor.WHITE + " - " + ChatColor.GRAY + reason);

            counter++;
        }

        result.close();
        resultCount.close();

        return true;

    }

    public boolean checkIfPlayerReportedBefore(String playername) throws SQLException {

        String query = "SELECT COUNT(*) AS count FROM reports WHERE reportedplayer = '" + playername + "'";
        ResultSet reportCount = plugin.dbManage.query(query);

        int count = reportCount.getInt("count");
        
        reportCount.close();

        if (count > 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkIfPlayerLoggingInReportedBefore(String playername) throws SQLException {

        String query = "SELECT COUNT(*) AS count FROM reports WHERE reportedplayer = '" + playername + "'";
        ResultSet reportCount = plugin.dbManage.query(query);

        int count = reportCount.getInt("count");
        
        reportCount.close();

        if (count >= 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteReport(CommandSender sender, String ID) {
        String query = "DELETE FROM 'reports' WHERE reportID = '" + ID + "'";
        plugin.dbManage.query(query);
        return true;
    }

    public boolean wipePlayerReports(CommandSender sender, String wipePlayer) {
        String query = "DELETE FROM 'reports' WHERE reportedplayer LIKE '" + wipePlayer + "'";
        plugin.dbManage.query(query);
        return true;
    }

    public int getNewestReportID(Player sender) throws SQLException {

        String query = "SELECT MAX(reportID) AS reportID FROM reports";
        ResultSet newestID = plugin.dbManage.query(query);

        int newestReportID = newestID.getInt("reportID");
        
        newestID.close();

        return newestReportID;
    }

    public boolean teleportToReport(Player player, int ID) throws SQLException {

        String query = "SELECT * FROM `reports` WHERE reportID = '" + ID + "' AND worldName IS NOT NULL";
        String getResultCount = "SELECT COUNT (*) AS count FROM `reports` WHERE reportID = '" + ID + "' AND worldName IS NOT NULL";
        ResultSet results = plugin.dbManage.query(query);
        ResultSet resultCount = plugin.dbManage.query(getResultCount);
        
        int numberOfResults = resultCount.getInt("count");
        
        if (numberOfResults < 1) {
            player.sendMessage(plugin.prefix + ChatColor.WHITE + "- " + ChatColor.RED + "No TP information is assigned with that report!");
            results.close();
            resultCount.close();
            return true;
        }

        while (results.next()) {

            Double xLoc = results.getDouble("xLoc");
            Double yLoc = results.getDouble("yLoc");
            Double zLoc = results.getDouble("zLoc");
            Float pitch = results.getFloat("pitch");
            Float yaw = results.getFloat("yaw");
            String worldName = results.getString("worldName");

            World world = plugin.getServer().getWorld(worldName);

            Location tpLoc = new Location(world, xLoc, yLoc, zLoc, yaw, pitch);

            player.teleport(tpLoc);

            player.sendMessage(plugin.prefix + ChatColor.WHITE + "- " + ChatColor.GREEN + "You have teleported to Report #" + ID);

        }
        
        results.close();
        resultCount.close();

        return true;
    }

    // SQLite Queries for update
    public void addTPLocColumns() throws SQLException {

        String createTempTable = "CREATE TABLE `TEMP_TABLE` (`id`, `reportID`, `reportdate`, `reporter`, `reportedplayer`, `reason`)";
        String backupReportsToTemp = "INSERT INTO `TEMP_TABLE` SELECT `id`, `reportID`, `reportdate`, `reporter`, `reportedplayer`, `reason` FROM `reports`";
        String removeOldReportTable = "DROP TABLE `reports`";
        String createNewReportTable = "CREATE TABLE IF NOT EXISTS `reports` (`id` int , `reportID` INT NOT NULL , `reportdate` VARCHAR(30) NOT NULL ,`reporter` VARCHAR( 30 ) NOT NULL ,`reportedplayer` VARCHAR( 30 ) NOT NULL ,`reason` TEXT NOT NULL ,`worldName` VARCHAR(255) NULL ,`xLoc` DOUBLE NULL ,`yLoc` DOUBLE NULL ,`zLoc` DOUBLE NULL ,`pitch` FLOAT NULL ,`yaw` FLOAT NULL)";
        String restoreReportsFromBackup = "INSERT INTO `reports` (`id`, `reportID`, `reportdate`, `reporter`, `reportedplayer`, `reason`) SELECT `id`, `reportID`, `reportdate`, `reporter`, `reportedplayer`, `reason` FROM `TEMP_TABLE`";
        String removeTempTable = "DROP TABLE `TEMP_TABLE`";
        plugin.dbManage.query(createTempTable);
        plugin.log.info("temp table created");
        plugin.dbManage.query(backupReportsToTemp);
        plugin.log.info("reports backed up");
        plugin.dbManage.query(removeOldReportTable);
        plugin.log.info("old table removed");
        plugin.dbManage.query(createNewReportTable);
        plugin.log.info("new table created");
        plugin.dbManage.query(restoreReportsFromBackup);
        plugin.log.info("reports restored from backup");
        plugin.dbManage.query(removeTempTable);
        plugin.log.info("temp table removed");

    }
}

