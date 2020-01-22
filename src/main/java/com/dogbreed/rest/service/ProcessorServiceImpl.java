package com.dogbreed.rest.service;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dogbreed.Exception.APIException;
import com.dogbreed.rest.model.DogBreed;
import com.dogbreed.util.ApplicationUtils;
import com.dogbreed.rest.service.DynamoDBService;
import com.dogbreed.rest.service.S3StorageService;

/*
 * Author Arvind R
 * 
 * This class is used to store the dog image and its details in DynamoDB and S3 Bucket
 */
@Service("processorServiceImpl")
public class ProcessorServiceImpl implements ProcessorService {


	private DogBreed dogBreed = null;

	@Value("${dog.breed.url}") 
	private String dogBreedURL;

	@Autowired
	private DynamoDBService dynamoDBService;
	
	@Autowired
	private S3StorageService s3StorageService;
	
	@Override
	/*
	 * This method is called to store the dog name, id, S3bucketURL and lastmodified timestamp in DynamoDB
	 * Stores the image as JPG in the S3 Bucket
	 */
	public DogBreed updateDogBreed(String message, InputStream inputStream) throws InterruptedException {

		String [] breed = ApplicationUtils.matches(message);
		String breedName=null;
		String id=null;

		if(breed != null) {
			breedName = breed[0];
			id = breed[1].split("\\.")[0];
		}

		dogBreed = new DogBreed();
		dogBreed.setName(breedName);
		dogBreed.setId(id);
		dogBreed.setLastmodified(new Date().toString());
		
		this.s3StorageService.getClient();
		String s3ImageURL = this.s3StorageService.uploadToS3(dogBreed.getName(), inputStream);
		dogBreed.setImageURL(s3ImageURL);
		
		this.dynamoDBService.save(dogBreed);

		return dogBreed;
	}
	
	@Override
	/*
	 * This method is to retrieve the dog details from DynamoDB by its id
	 */
	public DogBreed findByID(String id) throws Exception {
		
		try {
			dogBreed = this.dynamoDBService.getByID(id);
		} catch (Exception e) {
			throw new APIException(e.getMessage());
		}
		return dogBreed;
		
	}
	
	@Override
	/*
	 * This method is to delete the dog details from DynamoDB by its id
	 */
	public void deleteByID(String id) throws Exception {

		try {
			String breedName = this.dynamoDBService.delete(id);
			this.s3StorageService.getClient();
			this.s3StorageService.deleteFromS3(breedName);
		}
		catch(Exception e) {
			throw new APIException(e.getMessage());
		}
	}
	
	@Override
	/*
	 * This method is to get the dog details from DynamoDB by its name
	 */
	public List<DogBreed> getByName() throws Exception {
		List<DogBreed> dogBreedList = this.dynamoDBService.getByName();
		return dogBreedList;
	}

}
