package ru.retverd.jira.time.selfcontrol;

import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeSet;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ru.retverd.jira.time.selfcontrol.helpers.PeriodReports;
import ru.retverd.utilities.jira.rest.core.JiraClient;

import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.Worklog;

public class SelfController {
	public static void main(String[] args) throws Exception {
		ArrayList<BasicComponent> componentsCol;
		ArrayList<Worklog> workLogsCol;
		ArrayList<Version> moduleCol;
		Iterator<Worklog> worklogs;
		DateTime startDate;
		DateTime endDate;
		boolean issueCorrectness;
		int totalSearchResults;
		Iterator<Issue> issues;
		Issue issue;
		SearchResult searchResults;
		Worklog worklog;
		String pass;
		String login;

		int searchStep = 100;
		int searchPos = 0;
		InputStream input = null;
		String moduleName = "";
		String componentName = "";
		Properties prop = new Properties();
		PeriodReports reports = new PeriodReports();
		TreeSet<String> problems = new TreeSet<String>();
		ArrayList<String> users = new ArrayList<String>();
		DateTime today = new DateMidnight().toDateTime();
		DateTimeFormatter dateFormForReport = DateTimeFormat.forPattern("yyyy.MM.dd_HH_mm_ss");
		DateTimeFormatter dateDayOnly = DateTimeFormat.forPattern("EEEE");
		String enterLoginPrompt = "Please enter your login: ";
		String enterPassPrompt = "Please enter your password: ";

		try {
			input = new FileInputStream(args[0]);
			prop.load(input);
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		Scanner in = new Scanner(System.in);
		Console console = System.console();
		if (console == null) {
			System.out.print(enterLoginPrompt);
			login = in.next();
			System.out.print(enterPassPrompt);
			pass = in.next();
		} else {
			console.printf(enterLoginPrompt);
			login = console.readLine();
			console.printf(enterPassPrompt);
			pass = new String(console.readPassword());
		}

		System.out.format("Connecting to Jira...");
		JiraClient jc = new JiraClient(prop.getProperty("jira.url"), login, pass);
		System.out.format("done!%n");

		if (args.length == 1) {
			users.add(login);
		} else {
			for (int i = 1; i < args.length; i++) {
				users.add(args[i]);
			}
		}

		System.out.format("Please enter offset from today (e.g., -1 for %s) or start date (e.g., %s): ", JiraClient.getdateFormForReport()
				.print(today.minusDays(1)), JiraClient.getdateFormForReport().print(today.minusDays(1)));
		if (in.hasNextInt()) {
			startDate = today.plusDays(in.nextInt());
		} else {
			startDate = JiraClient.getdateFormForReport().parseDateTime(in.next());
		}
		System.out.println("Start date is " + JiraClient.getdateFormForReport().print(startDate) + " ("
				+ dateDayOnly.withLocale(Locale.ENGLISH).print(startDate) + ")");

		System.out.format("Please enter offset from today (e.g., 0 for %s) or end date (e.g., %s): ", JiraClient.getdateFormForReport()
				.print(today), JiraClient.getdateFormForReport().print(today));
		if (in.hasNextInt()) {
			endDate = today.plusDays(in.nextInt() + 1).minusSeconds(1);
		} else {
			endDate = JiraClient.getdateFormForReport().parseDateTime(in.next()).plusDays(1).minusSeconds(1);
		}
		System.out.println("End date is " + JiraClient.getdateFormForReport().print(endDate) + " ("
				+ dateDayOnly.withLocale(Locale.ENGLISH).print(endDate) + ")");

		System.out.format("Working with issues. Please wait it can take several minutes...");
		searchResults = jc.searchByJQLExpr("filter = " + prop.getProperty("jira.filter") + " AND updatedDate > "
				+ JiraClient.getDateFormForJira().print(startDate), searchStep, searchPos);
		totalSearchResults = searchResults.getTotal();

		if (totalSearchResults > 0) {
			do {
				issues = searchResults.getIssues().iterator();

				while (issues.hasNext()) {
					issue = ((Issue) issues.next());
					issueCorrectness = true;

					moduleCol = (ArrayList<Version>) issue.getFixVersions();
					if (moduleCol.isEmpty()) {
						problems.add("For issue " + issue.getKey() + " module is missing.");
						issueCorrectness = false;
					} else if (moduleCol.size() == 1) {
						moduleName = moduleCol.get(0).getName();
					} else {
						problems.add("For issue " + issue.getKey() + " assigned more than one module.");
						issueCorrectness = false;
					}

					componentsCol = (ArrayList<BasicComponent>) issue.getComponents();
					if (componentsCol.isEmpty()) {
						problems.add("For issue " + issue.getKey() + " component is missing.");
						issueCorrectness = false;
					} else if (componentsCol.size() == 1) {
						componentName = componentsCol.get(0).getName();
					} else {
						problems.add("For issue " + issue.getKey() + " assigned more than one component.");
						issueCorrectness = false;
					}

					if (issueCorrectness) {
						workLogsCol = (ArrayList<Worklog>) issue.getWorklogs();
						if (workLogsCol.size() == 20) {
							worklogs = jc.getWorklogs(issue);
						} else {
							worklogs = issue.getWorklogs().iterator();
						}

						while (worklogs.hasNext()) {
							worklog = ((Worklog) worklogs.next());
							if (users.contains(worklog.getAuthor().getName())) {
								if (worklog.getStartDate().isBefore(endDate) && worklog.getStartDate().isAfter(startDate)) {
									reports.addWorklog(issue, moduleName, componentName, worklog);
								}
							}
						}
					}
				}
				searchPos += searchStep;
				searchResults = jc.searchByJQLExpr("filter = " + prop.getProperty("jira.filter") + " AND updatedDate > "
						+ JiraClient.getDateFormForJira().print(startDate), searchStep, searchPos);
			} while (searchPos < totalSearchResults);
			System.out.format("done!%n");
			System.out.println("Processed " + totalSearchResults + " issue(s)!");

			if (problems.isEmpty()) {
				String filename = dateFormForReport.print(new DateTime()) + " report for period "
						+ JiraClient.getdateFormForReport().print(startDate) + " - " + JiraClient.getdateFormForReport().print(endDate)
						+ ".txt";
				PrintWriter writer = new PrintWriter(filename, "UTF-8");
				reports.savePersonReports(writer);
				writer.close();
				System.out.println("Report was saved to file \"" + filename + "\"");
			} else {
				System.out.println("Following errors were found:");
				for (String problem : problems) {
					System.out.println(problem);
				}
			}
		} else {
			System.out.format("done!%n");
		}
		jc.closeConnection();
		System.out.println("Hit Enter to exit... ");
		System.in.read();
		in.close();
	}
}
