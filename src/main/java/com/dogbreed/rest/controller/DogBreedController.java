package com.dogbreed.rest.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.dogbreed.Exception.APIException;
import com.dogbreed.Exception.NotFoundException;
import com.dogbreed.rest.model.DogBreed;
import com.dogbreed.rest.response.RestResponse;
import com.dogbreed.rest.response.RestTemplateResponse;
import com.dogbreed.rest.service.ProcessorService;

@RestController
public class DogBreedController extends BaseController {

	@Autowired
	@Qualifier("processorServiceImpl")
	private ProcessorService processorService;

	@Autowired
	RestTemplate restTemplate;

	@Value("${dog.breed.url}") 
	/*
	 *  Dog Breed URL which gives the breed details in a json format
	 */
	private String dogBreedURL;

	@Bean
	/*
	 *  Rest Template to call the public url to retrieve the details about a dog breed
	 */
	public RestTemplate rest() {
		return new RestTemplate();
	}

	/*
	 *  End point to retrieve the dog breed details, download the jpg, store it in DynamoDB and S3 and
	 *  renders the data in json format
	 *  
	 *  End point : http://localhost:8080/dogbreed
	 */
	@RequestMapping(path="/dogbreed", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DogBreed> getDogBreeds(HttpServletResponse response, InputStream inputStream) throws IOException, InterruptedException 
	{
		DogBreed dogBreed = null;
		RestTemplateResponse restTemplateResponse =
				restTemplate.getForObject(dogBreedURL, RestTemplateResponse.class);
		ResponseEntity<byte[]> responseEntity = null;

		try {
			responseEntity = downloadFile(restTemplateResponse.getMessage());
			inputStream = new ByteArrayInputStream(responseEntity.getBody());
			dogBreed = this.processorService.updateDogBreed(restTemplateResponse.getMessage(), inputStream);
		} catch (IOException e) {
			response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Something went wrong");
		}

		if(dogBreed == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			response.flushBuffer();
		}

		return ResponseEntity.accepted().body(dogBreed);
	}

	/*
	 *  End point to retrieve the dog breed by id from DynamoDB and
	 *  renders the data in json format
	 *  
	 *  End point : http://localhost:8080/dogbreed/<dogbreed-id>
	 */
	@ResponseBody
	@RequestMapping(path="/dogbreed/{dogBreedId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE) 
	public ResponseEntity<DogBreed> searchDogBreedByID(HttpServletResponse response, @PathVariable String dogBreedId)throws NotFoundException, APIException, IOException {

		DogBreed dogBreed = null;

		try {
			dogBreed = this.processorService.findByID(dogBreedId);
		} catch (Exception e) {
			response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Something went wrong");
		} 
		return ResponseEntity.accepted().body(dogBreed);
	}

	/*
	 *  End point to delete the dog breed by id from DynamoDB and S3, 
	 *  renders the response through the Response Entity object
	 *  
	 *  End point : http://localhost:8080/dogbreed/<dogbreed-id>
	 */
	@ResponseBody
	@RequestMapping(path="/dogbreed/{dogBreedId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE) 
	public ResponseEntity<RestResponse> deleteDogBreedByID(HttpServletResponse response , @PathVariable String dogBreedId)throws NotFoundException, APIException, IOException {

		try {
			this.processorService.deleteByID(dogBreedId);
		} catch (Exception e) {
			response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Something went wrong");
		}

		RestResponse restResponse = new RestResponse();
		restResponse.setStatusCode(HttpStatus.SC_OK);
		restResponse.setStatusMessage(dogBreedId + " deleted successfully from S3 and  DynamoDB");

		return ResponseEntity.ok().body(restResponse);

	}

	/*
	 *  End point to search the dog breed by name from DynamoDB, 
	 *  renders the response through the Response Entity object
	 *  
	 *  End point : http://localhost:8080/dogbreed/search?name=<dogbreed-id>
	 */
	@ResponseBody
	@RequestMapping(value="/dogbreed/search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<DogBreed>> searchDogBreed(HttpServletRequest request, HttpServletResponse response , @RequestParam String name) throws Exception {

		List<DogBreed> dogBreedList = this.processorService.getByName();
		dogBreedList = dogBreedList.stream().filter(a -> a.getName().equalsIgnoreCase(name)).collect(Collectors.toList());
		return ResponseEntity.accepted().body(dogBreedList);
	}

	/*
	 *  End point to retrieve all the dog breed names from DynamoDB, 
	 *  renders the response through the Response Entity object
	 *  
	 *  End point : http://localhost:8080/dogbreeds
	 */
	@ResponseBody
	@RequestMapping(value="/dogbreeds", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> getAllDogBreeds() throws Exception {
		List<String> nameList = new ArrayList<String>();
		List<DogBreed> dogBreedList = this.processorService.getByName();

		for(DogBreed breed: dogBreedList) {
			nameList.add(breed.getName());
		}
		return nameList;
	}

	/*
	 * This method downloads the dog jpg image and stores it in S3 
	 */
	public ResponseEntity<byte[]> downloadFile(String url) throws IOException {
		//This step is not necessary
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(null, headers);
		ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, request, byte[].class);
		return response;

	}
}
