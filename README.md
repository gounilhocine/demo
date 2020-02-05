package com.progteam.scvpaster.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CSVReader {
	static String csvFile = "c:/perso/company_paris.csv";
	static String url = "jdbc:mysql://localhost:3306progteamDB?useSSL=false";
	static String user = "root";
	static String password = "hocine2000";
	static Connection con;
	static Statement st;
	static String querySelect = "";

	public static void main(String[] args) {
		String query = "insert into company (name,address,postaCode,city,siret,country,webSite,industry,metricsAnnualProfit) values (";

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "UTF8"));
			ResultSet industries = st.executeQuery(querySelect);
			while ((line = br.readLine()) != null) {
				String[] company = line.split(cvsSplitBy);
				String name = company[0];
				query.concat(name + ",");
				String address = company[1] + " " + company[2];
				query.concat(address + ",");
				String postaCode = company[3];
				query.concat(postaCode + ",");
				String city = company[4];
				query.concat(city + ",");
				String siret = company[10];
				query.concat(siret + ",");
				String country = "52";
				query.concat(country + ",");
				String webSite = company[6];
				query.concat(webSite + ",");
				String industry = getIndustry(company[15], industries);
				query.concat(industry + ",");
				String metricsAnnualProfit = "";

				try {
					metricsAnnualProfit = company[21];
					query.concat(metricsAnnualProfit + ")");
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("city :" + 0 + " Euros");
				}

				try {
					con = DriverManager.getConnection(url, user, password);
					st = con.createStatement();
					boolean rs = st.execute(query);
					if (rs) {
						System.out.println("OK");
					}
				} catch (SQLException e) {
					System.out.println("company : " + name);
					e.printStackTrace();
				}

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param label
	 * @param industries
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	private static String getIndustry(String label, ResultSet industries) throws SQLException, IOException {
		Long industryId = null;
		if (industries.next()) {
			{
				String industry = industries.getString(2);
				if (industry.contains(label)) {
					industryId = industries.getLong(1);
				} else {
					InputStreamReader r = new InputStreamReader(System.in);
					BufferedReader br = new BufferedReader(r);
					System.out.println("Enter industry Id :");
					Long id = Long.valueOf(br.readLine());
					industryId = id;
				}
			}
		}
		return String.valueOf(industryId);
	}
}
