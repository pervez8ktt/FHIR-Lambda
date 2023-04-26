package com.example;

import java.io.IOException;

import org.json.JSONObject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;

/**
 * Hello world!
 *
 */
public class App implements RequestHandler<KinesisEvent, String> {
	public String handleRequest(final KinesisEvent event, final Context context) {
		for (KinesisEventRecord rec : event.getRecords()) {

			String data = new String(rec.getKinesis().getData().array());

			JSONObject jsonObject = new JSONObject(data);
//			int record_id = jsonObject.getInt("id");
			int systolicValue = jsonObject.getInt("systolic");
			int diastolicValue = jsonObject.getInt("diastolic");
			int pulseRate = jsonObject.getInt("pulse");
			String deviceId = jsonObject.getString("serial_number");

			final String fhirJson = FhirToJsonConverterNew.run("9813663", deviceId, systolicValue, diastolicValue, pulseRate);
			
			try {
				FhirToJsonConverterNew.postData(fhirJson);
				System.out.println("FHIR JSON posted to server: " + fhirJson);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
//			Thread t = new Thread(()->{
//				try {
//					FhirToJsonConverterNew.postData(fhirJson);
//					System.out.println("FHIR JSON posted to server: " + fhirJson);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			});
//			
//			t.start();
		}
		return "Hello from FHIR Lambda!";
	}
}
