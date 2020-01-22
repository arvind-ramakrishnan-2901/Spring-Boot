package com.dogbreed.rest.service;

import java.io.InputStream;

import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.dogbreed.Exception.APIException;
import com.dogbreed.util.Constants;


@Service("s3StorageService")
public class S3StorageServiceImpl  implements S3StorageService {
	
	private static AmazonS3Client amazonS3Client;
	
	@Value( "${aws.accessKey}" )
	String awsAccessKey;
	
	@Value( "${aws.secretKey}" )
	String awsSecretKey;
	
	/*
	 * This method is to instantiate the AmazonS3Client to store/retrieve/delete the dog details from S3
	 */
	public AmazonS3Client getClient() {
		
		if(amazonS3Client == null) {
			amazonS3Client = (AmazonS3Client)AmazonS3ClientBuilder.standard()
	                .withRegion(Regions.AP_SOUTHEAST_2)
	                .withCredentials(new AWSStaticCredentialsProvider(
	                 new BasicAWSCredentials(awsAccessKey,awsSecretKey)))
	                .build();
		}
		return amazonS3Client;
	}
		
	@Override
	/*
	 * This method is to upload the dog image to S3 Bucket
	 */
	public String uploadToS3(String fileName, InputStream inputStream) {
		ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(ContentType.APPLICATION_OCTET_STREAM.toString());
        objectMetadata.setCacheControl("public, max-age=31536000");
        amazonS3Client.putObject(new PutObjectRequest(Constants.S3_BUCKET_NAME, fileName, inputStream, objectMetadata).withCannedAcl(CannedAccessControlList.PublicRead));
        
        return amazonS3Client.getResourceUrl("dogbreed.pictures", fileName);
	}
	
	@Override
	/*
	 * This method is to delete the dog image from S3 Bucket
	 */
	public void deleteFromS3(String id) throws Exception {
		try {
			amazonS3Client.deleteObject(Constants.S3_BUCKET_NAME, id);
		}
		catch(AmazonServiceException amazonServiceException) {
			throw new APIException(amazonServiceException.getErrorCode()+amazonServiceException.getErrorMessage());
		}
	}
}
