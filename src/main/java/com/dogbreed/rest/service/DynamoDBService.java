package com.dogbreed.rest.service;

import java.util.List;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.dogbreed.rest.model.DogBreed;

public interface DynamoDBService {
	
	AmazonDynamoDB getClient() throws InterruptedException;
	
	void save(DogBreed dogBreed) throws InterruptedException;
	
	DogBreed getByID(String id) throws Exception;
	
	String delete(String id) throws Exception;
	
	List<DogBreed> getByName() throws Exception;
}
