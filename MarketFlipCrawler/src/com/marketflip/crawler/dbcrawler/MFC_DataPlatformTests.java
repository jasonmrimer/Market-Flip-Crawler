package com.marketflip.crawler.dbcrawler;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.marketflip.shared.products.MF_Price;
import com.marketflip.shared.products.MF_Product;


/**
 * Holds all tests for MFC_DataPlatform. To be moved to marketflip-tests project.
 * @author David
 *
 */
public class MFC_DataPlatformTests {
	
	private MFC_DataPlatform platform;
	private ExecutorService executor = Executors.newFixedThreadPool(1);
	Future<Boolean> future;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
		platform = new MFC_DataPlatform("testing");
	}

	@After
	public void tearDown() throws Exception {
		if (platform.getOperation().equals("insert")){
			
			//This is not deleting?? TODO: Figure out why it's not deleting "Product not contained in database.".
			platform.setOperation("delete");
			platform.call();
		}
		platform.close();
	}

	@Test
	public void Constructor_SendingTesting_ExpectTestingEnvironment() {
		assertTrue(platform.getEnvironment().equals("testing"));
	}
	
	@Test
	public void Constructor_SendingInvalidEnvironment_ExpectTestingEnvironment() {
		platform.close();
		platform = new MFC_DataPlatform("sadfsadfsdw");
		assertTrue(platform.getEnvironment().equals("testing"));
	}
	
	@Test
	public void Insert_SendingInvalidProduct_ExpectFalse () throws Exception {
		MF_Product product = new MF_Product("444444234", new ArrayList<MF_Price>());
		
		platform.setOperation("insert");
		platform.setProduct(product);
		assertFalse(platform.call());
	}
	
	@Test
	public void Insert_SendingValidProduct_ExpectTrue() throws Exception {
		
		ArrayList<MF_Price> priceList = new ArrayList<MF_Price>();
		priceList.add(new MF_Price(56.33, "Walmart"));
		MF_Product product = new MF_Product("0777733360571", priceList);
		
		platform.setOperation("insert");
		platform.setProduct(product);
		future = executor.submit(platform);
		int waitList = 0;
		while(!future.isDone()) {
			waitList++;
			System.out.println("waiting. " + waitList);
		}
		System.out.println("is done");
		assertTrue(future.get().booleanValue());
	}
}
