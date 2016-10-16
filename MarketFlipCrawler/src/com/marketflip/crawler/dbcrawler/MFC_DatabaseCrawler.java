
package com.marketflip.crawler.dbcrawler;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import com.marketflip.shared.data.MF_DataAccessObject;
import com.marketflip.shared.data.MF_ProductsDAO;
import com.marketflip.shared.products.MF_Product;
//
public class MFC_DatabaseCrawler implements Callable<Boolean> {
	private MF_DataAccessObject database;
	private MF_ProductsDAO productsDAO;
	private MF_Product product;
	
	// Construct with a product to insert into database
	public MFC_DatabaseCrawler(MF_DataAccessObject database, MF_Product product) {
		this.database = database;
		this.product = product;
	}

	@Override
	public Boolean call() throws SQLException {
		Boolean result = false;
		result = productsDAO.addProductToCommit(product);
//			result = database.insertProduct(product);
		return result;
	}

}
