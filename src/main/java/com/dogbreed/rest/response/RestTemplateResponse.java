package com.dogbreed.rest.response;

public class RestTemplateResponse {

		private String message;
	
	private String status;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		return "RestTemplateResponse [message=" + message + ", status=" + status + "]";
	}
}
