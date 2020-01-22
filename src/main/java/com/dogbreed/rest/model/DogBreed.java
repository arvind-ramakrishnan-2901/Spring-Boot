package com.dogbreed.rest.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@DynamoDBTable(tableName = "Dog")
@DynamoDBDocument
public class DogBreed {
	
	 private String id;
	 private String name;
	 private String imageURL;
	 private String lastmodified;

    public DogBreed() {

    }

    public DogBreed(String id, String name, String imageURL, String lastModified) {
        super();
        this.id = id;
        this.name = name;
        this.imageURL = imageURL;
        this.lastmodified = lastModified;
    }
 
    @DynamoDBHashKey(attributeName = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBAttribute(attributeName = "imageURL")
	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	@DynamoDBAttribute(attributeName = "lastmodified")
	public String getLastmodified() {
		return lastmodified;
	}

	public void setLastmodified(String lastmodified) {
		this.lastmodified = lastmodified;
	}

	@Override
    public String toString() {
        return "DogBreed [id=" + id + ", Name=" + name;
    }
}
