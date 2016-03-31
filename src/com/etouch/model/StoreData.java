package com.etouch.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


import com.etouch.beans.DocusignResults;
import com.etouch.service.HtppResponseHelper;


public class StoreData {

	public boolean store(DocusignResults res) {

		String envelope_id = res.getEnvelopeId();
		String document = res.getDocument();
		String status = res.getStatus();

		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection(HtppResponseHelper.mysqlUrl,HtppResponseHelper.mysqlUsername, HtppResponseHelper.mysqlPassword);

			PreparedStatement ps = con.prepareStatement("insert into `ConnsDevDB`.`docusign_results` (status, envelope_id, document, date, full_json, header) values (?, ?, ?, ?, ?, ?)");

			ps.setString(1, status);
			ps.setString(2, envelope_id);
			ps.setString(3, document);
			ps.setDate(4, new java.sql.Date(new java.util.Date().getTime()));
			ps.setString(5, res.getFullJson());
			ps.setString(6, res.getHeader());

			int i = ps.executeUpdate();
			if (i > 0) {
				System.out.println("Receved data from Docusign stored successfully...");
			}

		} catch (Exception d) {
			d.printStackTrace();
		}

		return true;
	}

	public List<DocusignResults> get() {

		List<DocusignResults> result = new ArrayList<DocusignResults>();

		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection(HtppResponseHelper.mysqlUrl,HtppResponseHelper.mysqlUsername, HtppResponseHelper.mysqlPassword);

			String query = "SELECT envelope_id, document, status, date, full_json, header FROM `ConnsDevDB`.`docusign_results` order by date desc limit 0, 10";

			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(query);

			// iterate through the java resultset
			while (rs.next()) {
				DocusignResults ds = new DocusignResults();
				
				ds.setEnvelopeId(rs.getString("envelope_id"));
				ds.setDocument(rs.getString("document"));
				ds.setStatus(rs.getString("status"));
				ds.setDate(rs.getDate("date"));
				ds.setFullJson(rs.getString("full_json"));
				ds.setHeader(rs.getString("header"));
				
				result.add(ds);
			}

		} catch (Exception d) {
			d.printStackTrace();
		}
		return result;
	}

}
