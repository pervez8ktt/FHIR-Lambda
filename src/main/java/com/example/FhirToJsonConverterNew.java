package com.example;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import org.hl7.fhir.r4.model.*;
public class FhirToJsonConverterNew {
	
	
	public static String run(String patientId, String deviceId, int systolicValue, int diastolicValue,
			int pulseRate) {
		FhirContext ctx = FhirContext.forR4();
		IParser parser = ctx.newJsonParser();

		Observation observation = new Observation();
		observation.setStatus(Observation.ObservationStatus.FINAL);

		CodeableConcept category = new CodeableConcept();
		category.addCoding().setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
				.setCode("vital-signs").setDisplay("Vital Signs");
		observation.addCategory(category);

		CodeableConcept code = new CodeableConcept();
		code.addCoding().setSystem("http://loinc.org").setCode("55284-4")
				.setDisplay("Blood pressure systolic and diastolic");
		code.setText("Blood Pressure");
		observation.setCode(code);

//		Reference subject = new Reference("Patient/" + patientId);
//		observation.setSubject(subject);

		observation.setEffective(new DateTimeType("2023-04-09T13:45:00-04:00"));

		Quantity systolicQuantity = new Quantity().setValue(systolicValue).setUnit("mmHg")
				.setSystem("http://unitsofmeasure.org").setCode("mm[Hg]");
		observation.addComponent().setCode(new CodeableConcept().addCoding(
				new Coding().setSystem("http://loinc.org").setCode("8480-6").setDisplay("Systolic blood pressure")))
				.setValue(systolicQuantity);

		Quantity diastolicQuantity = new Quantity().setValue(diastolicValue).setUnit("mmHg")
				.setSystem("http://unitsofmeasure.org").setCode("mm[Hg]");

		observation.addComponent().setCode(new CodeableConcept().addCoding(
				new Coding().setSystem("http://loinc.org").setCode("8462-4").setDisplay("Diastolic blood pressure")))
				.setValue(diastolicQuantity);

        // Set the device ID as a FHIR Identifier
        Identifier deviceIdentifier = new Identifier().setSystem("urn:example:device-id").setValue(deviceId);
        observation.addIdentifier(deviceIdentifier); 
        
		// Set the pulse value as a new FHIR Quantity
		observation.addComponent(new Observation.ObservationComponentComponent()
				.setCode(new CodeableConcept().addCoding(
						new Coding().setSystem("http://loinc.org").setCode("8867-4").setDisplay("Heart rate")))
				.setValue(new Quantity().setValue(pulseRate).setUnit("beats/minute")));

        
		FhirValidator validator = ctx.newValidator();
		ValidationResult result = validator.validateWithResult(observation);
		if (result.isSuccessful()) {
			System.out.println("Observation resource is valid for Record ID ." );
		} else {
			System.out.println("Observation resource is not valid for Record ID." );
			System.out.println(result.getMessages());
		}

		String fhirResourceJson = parser.encodeResourceToString(observation);
//		System.out.println("============FHIR Started ===================");
//		System.out.println(fhirResourceJson);
//		System.out.println("============FHIR Ends ===================");

		return fhirResourceJson;
	}

	public static void postData(String data) throws IOException {
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, data);
		Request request = new Request.Builder().url("http://hapi.fhir.org/baseR4/Observation").method("POST", body)
				.addHeader("Content-Type", "application/json").build();
		Response response = client.newCall(request).execute();

		String responseBody = response.body().string();
		System.out.println(responseBody);

	}

}