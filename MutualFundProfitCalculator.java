import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class MutualFundProfitCalculator {
    private static final String API_BASE_URL = "https://api.mfapi.in/mf/";

    public static double getNav(String schemeCode, String date) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(API_BASE_URL + schemeCode + "?date=" + date);
        HttpResponse response = httpClient.execute(request);

        if (response.getStatusLine().getStatusCode() == 200) {
            String responseBody = EntityUtils.toString(response.getEntity());
            JSONObject data = new JSONObject(responseBody);
            JSONArray navData = data.getJSONArray("data");
            return navData.getJSONObject(0).getDouble("nav");
        } else {
            throw new IOException("Unable to fetch NAV data.");
        }
    }

    public static double calculateProfit(String schemeCode, String startDate, String endDate, double capital) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date start = sdf.parse(startDate);
        Date end = sdf.parse(endDate);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);

     
        while (true) {
            try {
                double navStart = getNav(schemeCode, sdf.format(calendar.getTime()));
                break;
            } catch (IOException e) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

     
        double navEnd = getNav(schemeCode, endDate);


        double unitsAllotted = capital / navStart;


        double valueOnRedemption = unitsAllotted * navEnd;

       
        double netProfit = valueOnRedemption - capital;

        return netProfit;
    }

    public static void main(String[] args) {
        String schemeCode = "101206";
        String startDate = "26-07-2023";
        String endDate = "18-10-2023";
        double capital = 1000000.0;

        try {
            double profit = calculateProfit(schemeCode, startDate, endDate, capital);
            System.out.printf("Net Profit: ₹%.2f%n", profit);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
