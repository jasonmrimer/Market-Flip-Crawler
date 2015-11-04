package mfc_netcrawler;

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
	private String websitesTableName = "Websites";
	private String hostURL = "jdbc:mysql://173.194.234.96:3306/WebsiteURLs?user=root";
	private String username = "Jason";
	private String password = "marketflip";

	/**
	 * The no args constructor creates the connection to the database
	 * hardcoded into the class.  
	 */
	public MFC_TempDB() {
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
	/*
	 * Clean the database to start fresh, keeps tests clean to re-use
	 * base URLs that accept crawlers such as JSoup
	 * some code from: http://stackoverflow.com/questions/2942788/check-if-table-exists
	 */
	private void clearAllTables() throws SQLException{
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
	/*
	 * Create the table to hold websites
	 */
	private void createWebsitesTable(){
		String sql =
				"CREATE TABLE " + websitesTableName +
				"(URL varchar(255));";
		Statement sta;
		try {
			sta = con.createStatement();
			sta.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ResultSet getResultSet(String sql) {
		Statement sta;
		ResultSet resultSet = null;
		try {
			sta = con.createStatement();
			resultSet = sta.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultSet;
	}
	/**
	 * executes SQL INSERT, UPDATE, or DELETE statements returning success/failureå
	 * @param sql
	 * @return
	 */
	public boolean executePreparedStatement(String sql) {
		Statement statement = null;
		int executionResult = 0;
		try {
			statement = con.createStatement();
			executionResult = statement.executeUpdate(sql);
			if (statement != null) statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (executionResult > 0);
	}

	@Override
	protected void finalize() throws Throwable {
		if (con != null || !con.isClosed()) {
			con.close();
		}
	}
	/**
	 * This gives a result set to its caller in order to simplify SQL statements
	 * in other areas of the program and handle the errors in one place.
	 * @param startURL
	 * @return
	 * @throws SQLException
	 */
	public ResultSet getURLResultSet(String startURL) {
		ResultSet resultSet;
		resultSet = getResultSet(
					"SELECT ('" + startURL + "')" + 
					"FROM " + websitesTableName + ";");
		return resultSet;
	}
	/**
	 * simplify code readablility by taking URL and directly inserting it
	 * into the Website table. let the DB class take care of details
	 * @param attr
	 */
	public boolean insertURLToWebsiteTable(String URL) {
		// TODO Auto-generated method stub
		boolean didInsert = false;
		didInsert = executePreparedStatement(
							"INSERT INTO " + websitesTableName +
							" (URL) VALUES ('" + URL + "');");
		return didInsert;
	}
}
