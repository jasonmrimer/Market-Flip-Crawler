package mfc_dbcrawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.google.api.*;

/*
 * Temporary database class used until the MF_Shared version completes. Only used for
 * testing the NetCrawler and will use ultrabasic functions.
 * code lifted from: http://www.programcreek.com/2012/12/how-to-make-a-web-crawler-using-java/ ;
 * http://www.homeandlearn.co.uk/java/connect_to_a_database_using_java_code.html
 * https://cloud.google.com/appengine/docs/java/cloud-sql/
 */
public class MFC_TempDB {
	public Connection con = null;

	public MFC_TempDB() {
		String hostURL = "jdbc:google:mysql://marketflip-crawler:websites/WebsiteURLs";
		String username = "Jason";
		String password = "marketflip";

		try {
			Class.forName("com.mysql.jdbc.GoogleDriver");
			con = DriverManager.getConnection(hostURL, username, password);
			System.out.println("conn built");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
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
