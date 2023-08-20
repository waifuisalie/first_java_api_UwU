package weather;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Scanner;

public class APICaller {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("What city do you want to check? ");
        String user_city = scanner.nextLine();

        location_api_caller(user_city);

        
        
        scanner.close();
    }
    
    private static JSONArray generateJsonArrayForApiCall(String apiUrl) {
        HttpClient httpClient = HttpClients.createDefault();
        try {
            HttpGet request = new HttpGet(apiUrl);
            org.apache.http.HttpResponse response = httpClient.execute(request);
            String jsonRespose = EntityUtils.toString(response.getEntity());

            return new JSONArray(jsonRespose);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void location_api_caller(String user_city) {
        Scanner scanner = new Scanner(System.in);
        String apiKey = System.getenv("MY_API_KEY");

        String apiUrlFormat = "http://dataservice.accuweather.com/locations/v1/cities/search?apikey=%s&q=%s";
        String apiUrl = String.format(apiUrlFormat, apiKey, user_city);
        
        JSONArray jsonArray = generateJsonArrayForApiCall(apiUrl);

        if (jsonArray != null) {
            System.out.println("Available cities:");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject administrativeArea = jsonObject.getJSONObject("AdministrativeArea");
                JSONObject country = jsonObject.getJSONObject("Country");
                String state_name = administrativeArea.getString("LocalizedName");
                String country_name = country.getString("LocalizedName");
                
                System.out.println("(" + i + ") " + user_city + ", " + state_name + " from " + country_name);
            }

            System.out.print("Enter the index you wanna choose: ");
            int user_choice = scanner.nextInt();
            temp_api_caller(jsonArray, user_choice, user_city);

        } else {
            System.out.print("Error fetching data for locations.");
        }
        scanner.close();
        
    }
    
    private static void temp_api_caller(JSONArray jsonArray, int user_choice, String user_city) {
        String apiKey = System.getenv("MY_API_KEY");

        if (user_choice >= 0 && user_choice < jsonArray.length()) {
                JSONObject chosenObject = jsonArray.getJSONObject(user_choice);
                String chosenKey = chosenObject.getString("Key");

                String apiUrlFormatTemp = "http://dataservice.accuweather.com/currentconditions/v1/%s?apikey=%s";
                String apiUrlTemp = String.format(apiUrlFormatTemp, chosenKey, apiKey);

                JSONArray jsonArray2 = generateJsonArrayForApiCall(apiUrlTemp);

                if (jsonArray2 != null) {
                    for (int i = 0; i < jsonArray2.length(); i++) {
                        JSONObject jsonObject2 = jsonArray2.getJSONObject(i);
                        String weatherText = jsonObject2.getString("WeatherText");
                        JSONObject temperature = jsonObject2.getJSONObject("Temperature");
                        JSONObject metricObject = temperature.getJSONObject("Metric");
                        Double temperatureValue = metricObject.getDouble("Value");
                        String unit = metricObject.getString("Unit");
                        
                        System.out.println(user_city + " : " + weatherText + " and is " + temperatureValue + unit);
                    }   
                } else {
                    System.out.print("Error fetching data for temperatures.");
                }
            } else {
                System.out.println("Invalid choice.");
            }
    }
}
