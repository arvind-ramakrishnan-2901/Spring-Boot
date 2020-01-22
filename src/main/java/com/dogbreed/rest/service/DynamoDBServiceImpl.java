package com.dogbreed.rest.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.dogbreed.Exception.APIException;
import com.dogbreed.rest.model.DogBreed;
import com.dogbreed.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service("dynamoDBService")
public class DynamoDBServiceImpl implements DynamoDBService {

	private static AmazonDynamoDB dynamoDBClient;

	private static PutItemRequest request;
	
	private Table table;
	
	@Value("${aws.accessKey}")
	String awsAccessKey;

	@Value("${aws.secretKey}")
	String awsSecretKey;

	/*
	 * This method is to instantiate the AmazonDynamoDB to store/retrieve/delete the dog details from DynamoDB
	 */
	public AmazonDynamoDB getClient() throws InterruptedException {
		if(dynamoDBClient == null) {
			dynamoDBClient = AmazonDynamoDBClientBuilder
					.standard()
					.withRegion(Regions.AP_SOUTHEAST_2)
					.withCredentials(new AWSStaticCredentialsProvider(
							new BasicAWSCredentials(awsAccessKey,awsSecretKey)))
					.build();
			request = new PutItemRequest();
			request.setReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
			request.setReturnValues(ReturnValue.ALL_OLD);
		}

		return dynamoDBClient;
	}

	@Override
	/*
	 * This method is to save the dog details into DynamoDB
	 */
	public void save(DogBreed dogBreed) throws InterruptedException {

		this.getClient();
		DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
		table = createTableIfNotExist(dynamoDB);
		request.setTableName(table.getTableName());
		
		Map<String, AttributeValue> map = new HashMap<>();
		map.put("id", new AttributeValue(dogBreed.getId()));     
		map.put("name", (new AttributeValue(dogBreed.getName())));
		map.put("imageURL", new AttributeValue(dogBreed.getImageURL()));
		map.put("lastmodified", new AttributeValue(dogBreed.getLastmodified()));

		request.setItem(map);
		dynamoDBClient.putItem(request);
	}

	@Override
	/*
	 * This method is to retrieve the dog details by id from DynamoDB
	 */
	public DogBreed getByID(String id) throws Exception {

		this.getClient();
		
		DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
		table = createTableIfNotExist(dynamoDB);
		
		Item result = null;
		DogBreed dogBreed = null;
		GetItemSpec getItemSpec = new GetItemSpec().withPrimaryKey("id", id);

		try {
			result = table.getItem(getItemSpec);
			if(result != null) {
				ObjectMapper mapper = new ObjectMapper();
				dogBreed = mapper.readValue(result.toJSON(), DogBreed.class);
			}

		}
		catch(Exception e) {
			throw new APIException(e.getMessage());
		}
		return dogBreed;
	}

	@Override
	/*
	 * This method is to retrieve the dog details by name from DynamoDB
	 */
	public List<DogBreed> getByName() throws Exception {

		this.getClient();
		DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
		table = createTableIfNotExist(dynamoDB);
		request.setTableName(table.getTableName());
		
		List<DogBreed> dogBreedList = new ArrayList<DogBreed>();

		try {
			ScanRequest scanRequest = new ScanRequest().withTableName(Constants.TABLE_NAME);
			ScanResult result = null;

			do {
				if(result != null) {
					scanRequest.setExclusiveStartKey(result.getLastEvaluatedKey());
				}

				result = dynamoDBClient.scan(scanRequest);
				List<Map<String, AttributeValue>> rows = result.getItems();

				for(Map<String, AttributeValue> mapDogBreedRecord: rows) {
					DogBreed dogBreed = parseData(mapDogBreedRecord);
					dogBreedList.add(dogBreed);
				}

			} while(result.getLastEvaluatedKey() != null);
		}
		catch(AmazonClientException exception) {
			throw new APIException(exception.getMessage());
		}
		return dogBreedList;
	}

	@Override
	/*
	 * This method is to delete the dog details by id from DynamoDB
	 */
	public String delete(String id) throws Exception {

		this.getClient();
		DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
		table = createTableIfNotExist(dynamoDB);

		HashMap<String, AttributeValue> valueToDelete = new HashMap<String, AttributeValue>();
		valueToDelete.put("id", new AttributeValue(id));

		String breedName = getByID(id).getName();

		try {
			dynamoDBClient.deleteItem(table.getTableName(), valueToDelete).getSdkHttpMetadata().getHttpStatusCode();

		}
		catch(AmazonServiceException amazonServiceException) {
			throw new APIException(amazonServiceException.getErrorCode() + amazonServiceException.getErrorMessage());
		}

		return breedName;
	}

	private DogBreed parseData(Map<String, AttributeValue> dogBreedRecord) throws Exception {

		DogBreed dogBreed = new DogBreed();

		try {
			AttributeValue idAttributeValue = dogBreedRecord.get("id");
			AttributeValue nameAttributeValue = dogBreedRecord.get("name");
			AttributeValue imageUrlAttributeValue = dogBreedRecord.get("imageURL");
			AttributeValue dateAttributeValue = dogBreedRecord.get("lastmodified");

			dogBreed.setId(String.valueOf(idAttributeValue.getS()));

			if(nameAttributeValue != null) {
				dogBreed.setName(dogBreedRecord.get("name").getS());
			}

			if(imageUrlAttributeValue != null) {
				dogBreed.setImageURL(dogBreedRecord.get("imageURL").getS());
			}

			if(dateAttributeValue != null) {
				dogBreed.setLastmodified(dogBreedRecord.get("lastmodified").getS());
			}
		}
		catch(Exception e) {
			throw new APIException(e.getMessage());
		}
		return dogBreed;
	}
	
	/*
	 * This method is to create the dog table in DynamoDB if it doesn't exist
	 */
	private Table createTableIfNotExist(DynamoDB dynamoDB) throws InterruptedException {
		
		try {
			TableDescription tableDescription = dynamoDB.getTable(Constants.TABLE_NAME).describe();
			table = dynamoDB.getTable(Constants.TABLE_NAME);
		}
		catch(Exception e) {
			ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();

			ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
			keySchema.add(new KeySchemaElement().withAttributeName("id").withKeyType(KeyType.HASH));

			attributeDefinitions.add(new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S));

			CreateTableRequest request = new CreateTableRequest().withTableName(Constants.TABLE_NAME).withKeySchema(keySchema)
					.withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(5L)
							.withWriteCapacityUnits(5L));

			request.setAttributeDefinitions(attributeDefinitions);
			table = dynamoDB.createTable(request);
		}
		return table;
	}
}
