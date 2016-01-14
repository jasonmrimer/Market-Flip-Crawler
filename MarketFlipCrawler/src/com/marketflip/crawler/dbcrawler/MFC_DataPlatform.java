package com.marketflip.crawler.dbcrawler;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.concurrent.Callable;

import com.marketflip.shared.data.MF_ProductsDAO;
import com.marketflip.shared.products.MF_Product;

/**
 * The platform that controls access to the database. TODO: Will also control the local CouchDB instance to ensure that
 * the data is not dumped in case of a database or crawler failure.
 * @author David Walters
 * Last updated: 12/26/2015
 */

public class MFC_DataPlatform implements Callable<Boolean> {
	
	private final static int MAX_INSERT_OPERATIONS = 1; // 1 Insert is acceptable for now.
	
	private MF_ProductsDAO insertDAO;
	private MF_ProductsDAO deleteDAO;
	private MF_ProductsDAO getDAO;
	private HashSet<String> productSet = new HashSet<String> ();
	private String environment;
	private String operation;
	private MF_Product product;

	/**
	 * Creates a Data platform that manages various calls to the database. 
	 * @param environment The environment, either testing or production.
	 * @throws Exception 
	 */
	public MFC_DataPlatform (String environment) throws Exception {
		
		if (!environment.equals("testing") && !environment.equals("production")){
			System.err.println("ERROR: Invalid environment. Using test environment.");
			this.environment = "testing";
		} else {
			this.environment = environment;
		}
		this.insertDAO = new MF_ProductsDAO(this.environment);
		// TODO clear for testing
		insertDAO.deleteAllTables();
		insertDAO.addProductsTable();
		this.deleteDAO = new MF_ProductsDAO(this.environment);
		this.getDAO = new MF_ProductsDAO(this.environment);
		
		productSet = this.getDAO.getUpdatedProductList();
	}

	@Override
	public Boolean call() throws Exception {

		try {
			if (this.operation == null) {
				System.err.println("ERROR: Set operation first.");
				return false;
			} else if (this.product == null) {
				System.err.println("ERROR: Set a product first.");
				return false;
			}
			
			if (this.operation.equals("insert")){
				return insert(product);
			} else if (this.operation.equals("get")) {
				//Eventually we implement a 2 way BQ back to the crawler so that gets can be completed.
				//TODO: When a get method is needed this needs to be implemented.
				MF_Product productToReturn = get(product); 
			} else if (this.operation.equals("delete")) {
				return delete(product);
			} else {
				System.err.println("ERROR: Invalid operation");
				return false;
			}		
			return true;
		} catch (Exception e) {
			System.err.println("ERROR: Operation '" + operation + "' on product " + product.getUPC() + " failed.");
			return false;
		}
	}
	
	/**
	 * Sets the operation be performed. "insert" "delete" "get".
	 * @param operation The operation be performed.
	 */
	public void setOperation (String operation) {
		this.operation = operation;
	}
	
	/**
	 * Returns the current operation to be executed within the thread.
	 */
	public String getOperation () {
		return this.operation;
	}
	
	/**
	 * Sets the product to be operated upon.
	 * @param product The product to be inserted/get/delete.
	 */
	public void setProduct (MF_Product product) {
		this.product = product;
	}
	
	/**
	 * Returns the product being operated on.
	 */
	public MF_Product getProduct() {
		return this.product;
	}
	
	/**
	 * Returns the environment that the platform is running.
	 */
	public String getEnvironment() {
		return this.environment;
	}
	
	/**
	 * Inserts a product into the database.
	 * @param product The product to be inserted.
	 * @return boolean If the operation was successful.
	 * @throws SQLException
	 */
	private boolean insert (MF_Product product){
		
		if (productSet.contains(product.getUPC())) {
			System.err.println("ERROR: Product already contained in database.");
			return false;
		}
		try {
			boolean valid;
			valid = insertDAO.addProductToCommit(product);
			if (insertDAO.getCommitList().size() >= MAX_INSERT_OPERATIONS) {
				insertDAO.commitProductsToDatabase();
			}
			productSet.add(product.getUPC());
			return valid;
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * Gets a product from the database.
	 * @param product The product
	 * @return The full product.
	 * @throws SQLException
	 */
	private MF_Product get (MF_Product product) throws SQLException{
		
		if (!productSet.contains(product.getUPC())){
			System.err.println("ERROR: Product not contained in database.");
			return null;
		}
		return getDAO.getProduct(product);
	}
	/**
	 * Deletes a product from the database.
	 * @param product The product to be deleted.
	 * @return boolean If the operation was successful.
	 */
	private boolean delete (MF_Product product) {
		if (!productSet.contains(product)){
			System.err.println("ERROR: Product not contained in database.");
			return false;
		}
		try {
			return deleteDAO.delete(product);
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Closes all instances of the database. Must be called at thread termination.
	 */
	public void close() {
		insertDAO.close();
		getDAO.close();
		deleteDAO.close();
	}
	

}
