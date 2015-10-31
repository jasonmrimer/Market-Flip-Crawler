package mfc_dbcrawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.*;
import com.google.appengine.api.utils.SystemProperty;
/*
 * Temporary database class used until the MF_Shared version completes. Only used for
 * testing the NetCrawler and will use ultrabasic functions.
 * code lifted from: http://www.programcreek.com/2012/12/how-to-make-a-web-crawler-using-java/ ;
 * http://www.homeandlearn.co.uk/java/connect_to_a_database_using_java_code.html
 * https://cloud.google.com/appengine/docs/java/cloud-sql/
 * mvn archetype:generate -Dappengine-version=1.9.28 -Dapplication-id=your-app-id -Dfilter=com.google.appengine.archetypes:

 */
public class MFC_TempDB {
	public Connection con = null;

	public MFC_TempDB() {
		String hostURL = "jdbc:mysql://173.194.234.96:3306/WebsiteURLs?user=root";
		String username = "Jason";
		String password = "marketflip";

		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(hostURL, username, password);
			clearAllTables();
			createWebsitesTable();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	// TODO method to check if table exists
	private void clearAllTables() throws SQLException{
		/*
		 * some code from: http://stackoverflow.com/questions/2942788/check-if-table-exists
		 */
				
		// TODO method to clear all tables
		DatabaseMetaData meta = con.getMetaData();
		ResultSet rs = meta.getTables(null, null, "Websites", new String[] {"TABLE"});
		if (rs.next()){
			String sql = "DROP TABLE Websites";
			Statement sta;
			try {
				sta = con.createStatement();
				sta.execute(sql);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void createWebsitesTable(){
		// TODO method to create table
		String sql =
				"CREATE TABLE Websites (" +
				"URL varchar(255));";
		Statement sta;
		try {
			sta = con.createStatement();
			sta.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ResultSet runSql(String sql) throws SQLException {
		Statement sta = con.createStatement();
		return sta.executeQuery(sql);
	}

	public boolean runSql2(String sql) throws SQLException {
		Statement sta = con.createStatement();
		return sta.execute(sql);
	}

	@Override
	protected void finalize() throws Throwable {
		if (con != null || !con.isClosed()) {
			con.close();
		}
	}
}
