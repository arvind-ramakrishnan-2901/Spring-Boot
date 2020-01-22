package com.test.integration;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import com.dogbreed.rest.model.DogBreed;
import com.test.util.HttpClientUtil;
import com.test.util.TestConstants;

/*
 * Author Arvind R
 * Integration test case to test the rest end points
 */
public class DogBreedIT {
	
	private static HttpClientUtil httpUtil;
	
	@BeforeClass
	static public void setup() {
		httpUtil = new HttpClientUtil(DogBreed.class);
	}
	
	@Test
	public void testGetDogBreeds() throws Exception {
		HttpResponse httpResponse = httpUtil.executeGetRequest(TestConstants.DOGBREED_URL, null);
		DogBreed dogBreed = (DogBreed) httpUtil.getEntity(EntityUtils.toString(httpResponse.getEntity()));
		
		Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_ACCEPTED);
		Assert.assertNotNull(dogBreed);
		Assert.assertNotNull(dogBreed.getId());
	}
	
	@Test
	public void testGetDogBreed404() throws Exception {
		HttpResponse httpResponse = httpUtil.executeGetRequest(TestConstants.INCORRECT_DOGBREED_URL, null);
		DogBreed dogBreed = (DogBreed) httpUtil.getEntity(EntityUtils.toString(httpResponse.getEntity()));
		
		Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), HttpStatus.SC_NOT_FOUND);
		Assert.assertNull(dogBreed.getId());
	}
}
