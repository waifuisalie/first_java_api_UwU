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
        HttpClient httpClient = HttpClients.createDefault();
        String apiKey = "lol";

        Scanner scanner = new Scanner(System.in);
        System.out.print("What city do you want to check? ");
        String user_city = scanner.nextLine();

        String apiUrlFormat = "http://dataservice.accuweather.com/locations/v1/cities/search?apikey=%s&q=%s";
        String apiUrl = String.format(apiUrlFormat, apiKey, user_city);

        HttpGet request = new HttpGet(apiUrl);

        try {
            org.apache.http.HttpResponse response = httpClient.execute(request);
            String jsonResponse = EntityUtils.toString(response.getEntity());

            JSONArray jsonArray = new JSONArray(jsonResponse);

            System.out.println("Available cities:");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject administrativeArea = jsonObject.getJSONObject("AdministrativeArea");
                JSONObject country = jsonObject.getJSONObject("Country");
                String state_name = administrativeArea.getString("LocalizedName");
                String country_name = country.getString("LocalizedName");

                System.out.println("(" + i + ") " + user_city + ", " + state_name + " from " + country_name);
            }

            System.out.print("Enter the index that you want to choose: ");
            int user_choice = scanner.nextInt();

            if (user_choice >= 0 && user_choice < jsonArray.length()) {
                JSONObject chosenObject = jsonArray.getJSONObject(user_choice);
                String chosenKey = chosenObject.getString("Key");

                String apiUrlFormatTemp = "http://dataservice.accuweather.com/currentconditions/v1/%s?apikey=%s";
                String apiUrlTemp = String.format(apiUrlFormatTemp, chosenKey, apiKey);
                HttpGet requestTemp = new HttpGet(apiUrlTemp);

                org.apache.http.HttpResponse responseTemp = httpClient.execute(requestTemp);
                String jsonResponse2 = EntityUtils.toString(responseTemp.getEntity());

                JSONArray jsonArray2 = new JSONArray(jsonResponse2);

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
                System.out.println("Invalid choice.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
