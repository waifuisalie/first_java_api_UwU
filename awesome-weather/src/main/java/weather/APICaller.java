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
        APICaller apiCaller = new APICaller();
        apiCaller.run();
    }
    
    private static String locationApiUrl(String user_city) {
        String apiKey = System.getenv("MY_API_KEY");
        String apiUrlFormat = "http://dataservice.accuweather.com/locations/v1/cities/search?apikey=%s&q=%s";
        String locationApiUrl = String.format(apiUrlFormat, apiKey, user_city);
        return locationApiUrl;
    }
    
    private static String keyApiUrl(int user_choice) {
        String apiKey = System.getenv("MY_API_KEY");
        String apiUrlFormatTemp = "http://dataservice.accuweather.com/currentconditions/v1/%s?apikey=%s";
        String chosenKey = fetchChosenKey(user_choice, apiUrlFormatTemp);
        String apiUrlTemp = String.format(apiUrlFormatTemp, chosenKey, apiKey);
        return apiUrlTemp;
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

    private static void fetchLocationData(String user_city) {
        String apiUrl = locationApiUrl(user_city);
        JSONArray jsonArray = generateJsonArrayForApiCall(apiUrl);
        

        if (jsonArray != null) {
            System.out.println("Available cities:");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject administrativeArea = jsonObject.getJSONObject("AdministrativeArea");
                JSONObject country = jsonObject.getJSONObject("Country");
                String stateName = administrativeArea.getString("LocalizedName");
                String countryName = country.getString("LocalizedName");
                
                System.out.println("(" + i + ") " + user_city + ", " + stateName + " from " + countryName);
            }
        } else {
            System.out.print("Error fetching data for locations.");
        }        
    }

    private static String fetchChosenKey(int user_choice, String user_city) {
        String apiUrl = locationApiUrl(user_city);
        JSONArray jsonArray = generateJsonArrayForApiCall(apiUrl);
        
        if (user_choice >= 0 && user_choice < jsonArray.length()) {
                JSONObject chosenObject = jsonArray.getJSONObject(user_choice);
                String chosenKey = chosenObject.getString("Key");
                return chosenKey;
            } else {
                System.out.print("Invalid choice!");
                return null;
        }
    }

    private static void fetchTemperatureData(int user_choice, String user_city) {
        String chosen_key_string = fetchChosenKey(user_choice, user_city);
        int chosen_key_int = Integer.parseInt(chosen_key_string);
        String apiUrlTemp = keyApiUrl(chosen_key_int);
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
    }
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("What city do you want to check? ");
        String user_city = scanner.nextLine();

        fetchLocationData(user_city);

        System.out.print("Enter the index you wanna choose: ");
        int user_choice = scanner.nextInt();

        fetchTemperatureData(user_choice, user_city);

        scanner.close();
    }
}
