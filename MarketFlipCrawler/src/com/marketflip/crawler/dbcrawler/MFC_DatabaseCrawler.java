
package com.marketflip.crawler.dbcrawler;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import com.marketflip.shared.data.MF_DatabaseAccessObject;
import com.marketflip.shared.products.MF_Product;
//
public class MFC_DatabaseCrawler implements Callable<Boolean> {
	private MF_DatabaseAccessObject database;
	private MF_Product product;
	
	// Construct with a product to insert into database
	public MFC_DatabaseCrawler(MF_DatabaseAccessObject database, MF_Product product) {
		this.database = database;
		this.product = product;
	}

	@Override
	public Boolean call() {
		Boolean result = false;
		try {
			result = database.insertProduct(product);
		} catch (SQLException e) {
			// TODO Auto-generated catch block; move to insertProduct method
			System.err.println("DBCrawler did not insert.");
			e.printStackTrace();
		}
		return result;
	}

}
