package com.dogbreed.rest.service;

import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.dogbreed.rest.model.DogBreed;

@Repository
//public class DynamoDBService
public interface ProcessorService
{
	DogBreed updateDogBreed(String message, InputStream inputStream) throws InterruptedException;
	
	DogBreed findByID(String id) throws Exception;
	
	void deleteByID(String id) throws Exception;
	
	List<DogBreed> getByName() throws Exception;
}
