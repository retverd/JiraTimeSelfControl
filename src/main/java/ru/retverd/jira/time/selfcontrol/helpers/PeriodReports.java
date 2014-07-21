package ru.retverd.jira.time.selfcontrol.helpers;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import ru.retverd.utilities.jira.rest.core.JiraClient;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Worklog;

public class PeriodReports {
	private TreeMap<String, DateReport> dateReportsList;

	public PeriodReports() {
		dateReportsList = new TreeMap<String, DateReport>();
	}

	public void addWorklog(Issue issue, String moduleName, String componentName, Worklog worklog) throws Exception {
		String date = JiraClient.getdateFormForReport().print(worklog.getStartDate());
		if (!dateReportsList.containsKey(date)) {
			dateReportsList.put(date, new DateReport());
		}
		dateReportsList.get(date).addWorklog(issue, moduleName, componentName, worklog);
	}

	public void savePersonReports(PrintWriter writer) {
		if (dateReportsList.size() == 0) {
			writer.println("No data found for specified period!");
		} else {
			for (Map.Entry<String, DateReport> entry : dateReportsList.entrySet()) {
				writer.println("Date " + entry.getKey());
				entry.getValue().saveDateReport(writer);
			}
		}
	}
}