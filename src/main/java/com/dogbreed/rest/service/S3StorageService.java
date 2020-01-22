package com.dogbreed.rest.service;

import java.io.InputStream;

import com.amazonaws.services.s3.AmazonS3Client;

public interface S3StorageService {
	
	AmazonS3Client getClient();

	String uploadToS3(String fileName, InputStream inputStream);
	
	void deleteFromS3(String id) throws Exception;
}
