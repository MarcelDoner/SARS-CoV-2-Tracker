package me.marceldoner.covidtracker.controllers;

import me.marceldoner.covidtracker.models.LocationStats;
import me.marceldoner.covidtracker.services.CoronaVirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private CoronaVirusDataService coronaVirusDataService;

    @GetMapping("/")
    public String home(Model model) {
        List<LocationStats> allStats = coronaVirusDataService.getAllStats();

        int totalReportedInfected = allStats.stream().mapToInt(LocationStats::getLatestTotalCases).sum();
        int totalNewInfected = allStats.stream().mapToInt(LocationStats::getDiffFromPrevDay).sum();

        model.addAttribute("totalReportedCases", totalReportedInfected);
        model.addAttribute("totalNewCases", totalNewInfected);

        return "home";
    }
}
