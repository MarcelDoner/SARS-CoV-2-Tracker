package me.marceldoner.covidtracker.services;

import me.marceldoner.covidtracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {

    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")
    public void fetchVirusData() {
        HttpResponse<String> httpResponse = getStringHttpResponse();
        Iterable<CSVRecord> records = getCsvRecords(httpResponse);
        List<LocationStats> newStats = new ArrayList<>();
        updatePage(records, newStats);
        this.allStats = newStats;
    }

    private void updatePage(Iterable<CSVRecord> records, List<LocationStats> newStats) {
        for (CSVRecord record : records) {
            LocationStats locationStat = new LocationStats();
            int latestCases = Integer.parseInt(record.get(record.size() - 1));
            int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
            locationStat.setLatestTotalCases(latestCases);
            locationStat.setDiffFromPrevDay(latestCases - prevDayCases);
            newStats.add(locationStat);
        }
    }

    private HttpResponse<String> getStringHttpResponse() {
        final String SARSCoV2_Data_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(SARSCoV2_Data_URL)).build();
        HttpResponse<String> httpResponse = null;
        HttpClient client = HttpClient.newHttpClient();
        try {
            httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return httpResponse;
    }

    private Iterable<CSVRecord> getCsvRecords(HttpResponse<String> httpResponse) {
        StringReader csvReader = getStringReader(httpResponse);
        Iterable<CSVRecord> records = null;
        try {
            records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

    private StringReader getStringReader(HttpResponse<String> httpResponse) {
        return new StringReader(httpResponse.body());
    }

}