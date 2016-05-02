package uk.co.HariboPenguin.uReport;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportDataHandlerMySQL {

    public Report plugin;

    public ReportDataHandlerMySQL(Report instance) {
        this.plugin = instance;
    }

    public boolean sendReport(String reporter, String reportedPlayer, String reason, String textDate, Integer ReportID) throws SQLException {
        
        String database = plugin.getConfig().getString("mysql.database");
                       
        String query = "INSERT INTO `" + database + "`.`reports` (`reportID` ,`reportdate` ,`reporter` ,`reportedplayer` ,`reason`) VALUES ('" + ReportID + "', '" + textDate + "',  '" + reporter + "',  '" + reportedPlayer + "',  '" + reason + "')";
        plugin.dbManageMySQL.query(query);
        return true;

    }
    
    public boolean sendReportWithTPInfo(String reporter, String reportedPlayer, String reason, String textDate, Integer reportID, String worldName, Double xLoc, Double yLoc, Double zLoc, Float pitch, Float yaw) throws SQLException {
        
        String database = plugin.getConfig().getString("mysql.database");
        
        String query = "INSERT INTO  `" + database + "`.`reports` (`reportID` ,`reportdate` ,`reporter` ,`reportedplayer` ,`reason` ,`worldName` ,`xLoc` ,`yLoc` ,`zLoc` ,`pitch` ,`yaw`) VALUES ('" + reportID + "', '" + textDate + "',  '" + reporter + "',  '" + reportedPlayer + "',  '" + reason + "', '" + worldName + "', '" + xLoc + "', '" + yLoc + "', '" + zLoc + "', '" + pitch + "', '" + yaw + "')";
        plugin.dbManage.query(query);
        return true;

    }

    public boolean getReports(CommandSender sender, String playername, int pageNumber) throws SQLException {
        
        String database = plugin.getConfig().getString("mysql.database");
        
        String query = "SELECT * FROM `" + database + "`.`reports` WHERE  `reportedplayer` LIKE '" + playername + "' ORDER BY `reports`.`reportID` DESC";
        String getNumberOfReports = "SELECT COUNT(*) AS count FROM `" + database + "`.`reports` WHERE reportedplayer LIKE '" + playername + "'";
        ResultSet result = plugin.dbManageMySQL.query(query);
        ResultSet resultCount = plugin.dbManageMySQL.query(getNumberOfReports);
        
        resultCount.first();

        int numberOfResults = resultCount.getInt("count");

        int resultsPerPage = plugin.getConfig().getInt("reports-per-page");

        int pageCount = Math.round((int) Math.ceil((numberOfResults - 1) / resultsPerPage)) + 1;

        if (pageNumber > pageCount) {
            sender.sendMessage(plugin.prefix + ChatColor.GOLD + "- " + ChatColor.RED + "That page does not exist!");
            return true;
        }

        if (numberOfResults == 0) {
            sender.sendMessage(plugin.prefix + ChatColor.GOLD + "- " + ChatColor.RED + "No Reports Found!");
            return true;
        }

        sender.sendMessage(ChatColor.DARK_PURPLE + "---------- " + ChatColor.GOLD + "uReport - Report for " + playername + ChatColor.DARK_PURPLE + " ----------");
        sender.sendMessage(ChatColor.DARK_PURPLE + "-------------------- " + ChatColor.GOLD + "Page " + pageNumber + " / " + pageCount + ChatColor.DARK_PURPLE + " ---------------------");

        if (result == null) {
            plugin.log.info("No results found");
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

            sender.sendMessage(ChatColor.GOLD + "#" + reportid + " " + reportdate + " by " + ChatColor.RED + reporter + ChatColor.GOLD + " - " + ChatColor.GRAY + reason);

            counter++;

            //	if (result.isLast()) break;
        }
        
        result.close();
        resultCount.close();
        
        return true;

    }

    public boolean getLatestReports(CommandSender sender, String playername, int pageNumber) throws SQLException {
        
        String database = plugin.getConfig().getString("mysql.database");
        
        String query = "SELECT * FROM `" + database + "`.`reports` WHERE  `reportedplayer` IS NOT NULL ORDER BY `reports`.`reportID` DESC";
        String getNumberOfReports = "SELECT COUNT(*) AS count FROM `" + database + "`.`reports` WHERE reportedplayer IS NOT NULL";
        ResultSet result = plugin.dbManageMySQL.query(query);
        ResultSet resultCount = plugin.dbManageMySQL.query(getNumberOfReports);
        
        resultCount.first();

        int numberOfResults = resultCount.getInt("count");

        int resultsPerPage = plugin.getConfig().getInt("reports-per-page");

        int pageCount = Math.round((int) Math.ceil((numberOfResults - 1) / resultsPerPage)) + 1;

        if (pageNumber > pageCount) {
            sender.sendMessage(plugin.prefix + ChatColor.GOLD + "- " + ChatColor.RED + "That page does not exist!");
            return true;
        }

        if (numberOfResults == 0) {
            sender.sendMessage(plugin.prefix + ChatColor.GOLD + "- " + ChatColor.RED + "No Reports Found!");
            return true;
        }

        sender.sendMessage(ChatColor.DARK_PURPLE + "--------------" + ChatColor.GOLD + " uReport " + ChatColor.GOLD + "- Latest Reports " + ChatColor.DARK_PURPLE + " --------------");
        sender.sendMessage(ChatColor.DARK_PURPLE + "-------------------- " + ChatColor.GOLD + "Page " + pageNumber + " / " + pageCount + ChatColor.DARK_PURPLE + " ---------------------");

        if (result == null) {
            plugin.log.info("No results found");
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

            sender.sendMessage(ChatColor.GOLD + "#" + reportid + " " + reportdate + " - " + ChatColor.RED + reportedplayer + ChatColor.GOLD + " - " + ChatColor.GRAY + reason);

            counter++;
        }
        
        result.close();
        resultCount.close();
        
        return true;

    }

    public boolean checkIfPlayerReportedBefore(String playername) throws SQLException {

        String database = plugin.getConfig().getString("mysql.database");
        
        String query = "SELECT COUNT(*) AS count FROM `" + database + "`.`reports` WHERE reportedplayer = '" + playername + "'";
        ResultSet reportCount = plugin.dbManageMySQL.query(query);
        
        reportCount.first();

        int count = reportCount.getInt("count");
        
        if (count > 1) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean checkIfPlayerLoggingInReportedBefore(String playername) throws SQLException {

        String database = plugin.getConfig().getString("mysql.database");
        
        String query = "SELECT COUNT(*) AS count FROM `" + database + "`.`reports` WHERE reportedplayer = '" + playername + "'";
        ResultSet reportCount = plugin.dbManageMySQL.query(query);
        
        reportCount.first();

        int count = reportCount.getInt("count");
        
        if (count >= 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteReport(CommandSender sender, String ID) {
        
        String database = plugin.getConfig().getString("mysql.database");
        
        String query = "DELETE FROM `" + database + "`.`reports` WHERE reportID = '" + ID + "'";
        plugin.dbManageMySQL.query(query);
        return true;
    }

    public boolean wipePlayerReports(CommandSender sender, String wipePlayer) {
        
        String database = plugin.getConfig().getString("mysql.database");
        
        String query = "DELETE FROM `" + database + "`.`reports` WHERE reportedplayer LIKE '" + wipePlayer + "'";
        plugin.dbManageMySQL.query(query);
        return true;
    }

    public int getNewestReportID(Player sender) throws SQLException {

        String database = plugin.getConfig().getString("mysql.database");
        
        String query = "SELECT MAX(reportID) AS reportID FROM `" + database + "`.`reports`";
        ResultSet newestID = plugin.dbManageMySQL.query(query);
        
        newestID.first();

        int newestReportID = newestID.getInt("reportID");

        return newestReportID;
    }
    
    public boolean teleportToReport(Player player, int ID) throws SQLException {

        String database = plugin.getConfig().getString("mysql.database");
        
        String query = "SELECT * FROM `" + database + "`.`reports` WHERE reportID = '" + ID + "' AND worldName IS NOT NULL";
        String getResultCount = "SELECT COUNT (*) AS count FROM `" + database + "`.`reports` WHERE reportID = '" + ID + "' AND worldName IS NOT NULL";
        ResultSet results = plugin.dbManage.query(query);
        ResultSet resultCount = plugin.dbManage.query(getResultCount);
        
        int numberOfResults = resultCount.getInt("count");
        
        if (numberOfResults < 1) {
            player.sendMessage(plugin.prefix + ChatColor.GOLD + "- " + ChatColor.RED + "No TP information is assigned with that report!");
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

            player.sendMessage(plugin.prefix + ChatColor.GOLD + "- " + ChatColor.GREEN + "You have teleported to Report #" + ID);

        }

        return true;
    }
    
    public void addTPLocColumns() throws SQLException {
        
        String database = plugin.getConfig().getString("mysql.database");
        
        String query = "ALTER TABLE `" + database + "`.`reports` ADD COLUMN `world` VARCHAR(255) NULL  AFTER `reason` , ADD COLUMN `xLoc` DOUBLE NULL  AFTER `world` , ADD COLUMN `yLoc` DOUBLE NULL  AFTER `xLoc` , ADD COLUMN `zLoc` DOUBLE NULL  AFTER `yLoc` , ADD COLUMN `pitch` FLOAT NULL  AFTER `zLoc` , ADD COLUMN `yaw` FLOAT NULL  AFTER `pitch`";
        plugin.dbManageMySQL.query(query);
        
    }
    public void databaseKeepAlive() throws SQLException {
        
        String query = "SELECT 0";
        plugin.dbManageMySQL.query(query);
    }
    
}

