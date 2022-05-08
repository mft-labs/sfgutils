
package com.mftlabs.sfgutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.stringtree.json.JSONReader;

import com.mftlabs.sfgutils.bpexec.KhkParameters;
import com.sterlingcommerce.woodstock.util.frame.jdbc.JDBCService;
import com.sterlingcommerce.woodstock.workflow.WorkFlowContext;


public class SfgApiClient {
	private static HashMap<String, String> config = null;

	public static HashMap<String, String> GetConfig() {
		String sfgHome = System.getenv().get("AMF_SFG_HOME");
		File file = new File(sfgHome + "/properties/sfgutils.properties");
		HashMap<String, String> dict = new HashMap<String, String>();
		Scanner sc = null;
		try {
			sc = new Scanner(file);
			while (sc.hasNextLine()) {
				String text = sc.nextLine();
				text = text.trim();
				if (text.startsWith("#") || text.length()==0) {
					continue;
				}
				String[] arr = text.split("=");
				String key = arr[0].trim();
				String[] arr2 = arr[1].split("#");
				String value = arr2[0].trim();
				dict.put(key, value);
			}
		} catch (FileNotFoundException e) {
			return null;
		} finally {
			if(sc!=null) {
				sc.close();
			}
		}
		return dict;

	}

	public static HashMap<String,Object> isTradingPartnerGM(String partner) throws Exception {
		HashMap<String, String> config = GetConfig();
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		StringWriter sw = new StringWriter();
		if (config != null) {
			String apiUrl = config.get("SFG_API_BASEURL");
			String apiUser = config.get("SFG_SI_USERNAME");
			String apiPasswd = config.get("SFG_SI_PASSWORD");
			sw.write("Connecting Api with "+apiUrl+"\n");
			String auth = apiUser + ":" + apiPasswd;
			byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
			String authHeaderValue = "Basic " + new String(encodedAuth);
			System.out.println(authHeaderValue);
			
			URL url = new URL(apiUrl + "/B2BAPIs/svc/tradingpartners/?searchFor="+partner);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", authHeaderValue);
			con.setConnectTimeout(15000);
			con.setDoOutput(true);
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			JSONReader reader = new JSONReader();
			ArrayList result = (ArrayList)reader.read(content.toString());
			
			HashMap<String,Object> user = (HashMap<String,Object>)result.get(0);
			HashMap<String, Object> useGlobalMailbox = (HashMap<String,Object>)user.get("useGlobalMailbox");
			sw.write("Retrieved code as "+useGlobalMailbox.get("code").toString()+"\n");
			resultMap.put("GMUser",useGlobalMailbox.get("code").toString());
			resultMap.put("details", sw.toString());
			return resultMap;
		}
		return null;
	}
	
	public static HashMap<String,Object> isGMUser(String partner) throws Exception {
		Connection con = null;
	    PreparedStatement ps = null;
	    ResultSet rs = null;
	    String query = "select ORGANIZATION_CODE from YFS_ORGANIZATION  where OBJECT_ID in (select ENTITY_ID from SCI_ENTITY_EXTNS where EXTENSION_KEY = 'PARTNER_USES_DIST_MAILBOX') and ORGANIZATION_CODE='"+partner+"'";
	    HashMap<String, String> config = GetConfig();
	    try {
			HashMap<String,Object> result = new HashMap<String,Object>();
			 String dbPool = config.get("SFG_DBPOOL");
			 boolean gmUser = false;
		      con = JDBCService.getConnection(dbPool);
		      con.setAutoCommit(true);
		      ps = con.prepareStatement(query);
		      rs = ps.executeQuery();
		      StringWriter sw = new StringWriter();
		      
		      while(rs.next()) {
		    	  sw.write("Found partner "+partner+"\n");
		    	  String orgCode = rs.getString("ORGANIZATION_CODE").trim();
		    	  if (orgCode.equalsIgnoreCase(partner)) {
		    		  sw.write("Organization code matched "+orgCode+" == "+partner);
		    		  gmUser=true;
			    	  break;
		    	  }
		    	  
		      }
		      if (!gmUser) {
		    	  sw.write("Partner "+partner+" is not a GM User");
		      }
		      result.put("GMUser",gmUser);
		      result.put("details",sw.toString());
		      return result;
		} catch(Exception e) {
			RuntimeException e2 = new RuntimeException("Failed to retrieve partner information "+e.getMessage());
			throw e2;
		}
	}
	
	public static String addKnownHostKey(WorkFlowContext wfc,KhkParameters khkParams) throws Exception {
		HashMap<String, String> config = GetConfig();
		if (config != null) {
			String apiUrl = config.get("SFG_API_BASEURL");
			String apiUser = config.get("SFG_SI_USERNAME");
			String apiPasswd = config.get("SFG_SI_PASSWORD");
			
			String auth = apiUser + ":" + apiPasswd;
			byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
			String authHeaderValue = "Basic " + new String(encodedAuth);
			System.out.println(authHeaderValue);
			
			URL url = new URL(apiUrl + "/B2BAPIs/svc/sshknownhostkeys/");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", authHeaderValue);
			con.setConnectTimeout(15000);
			con.setDoOutput(true);
			
			byte[] encoded = Base64.getEncoder().encode(khkParams.getSshKeyData().getBytes(StandardCharsets.UTF_8));
			String encodedStr = new String(encoded);
			
			String outputStr = "{"
								+ "\"keyData\": \""+encodedStr+"\","
								+ "\"keyName\":\"" +khkParams.getKhkName()+"\","
								+ "\"keyStatusEnabled\":true"
								+ "}";
			byte[] outputBytes = outputStr.getBytes("UTF-8");
			OutputStream os = con.getOutputStream();
			os.write(outputBytes);

			os.close();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			
			
			return content.toString();
		}
		return null;
	}
	
	public static String addSshRemoteProfile(WorkFlowContext wfc,KhkParameters khkParams) throws Exception {
		HashMap<String, String> config = GetConfig();
		if (config != null) {
			String apiUrl = config.get("SFG_API_BASEURL");
			String apiUser = config.get("SFG_SI_USERNAME");
			String apiPasswd = config.get("SFG_SI_PASSWORD");
			
			String auth = apiUser + ":" + apiPasswd;
			byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
			String authHeaderValue = "Basic " + new String(encodedAuth);
			System.out.println(authHeaderValue);
			
			URL url = new URL(apiUrl + "/B2BAPIs/svc/sshremoteprofiles/");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", authHeaderValue);
			con.setConnectTimeout(15000);
			con.setDoOutput(true);
			
			String outputStr = "{"
					+ "\"characterEncoding\": null,\n" + 
					"  \"compression\": null,\n" + 
					"  \"connectionRetryCount\": null,\n" + 
					"  \"directory\": null,\n" + 
					"  \"knownHostKeys\": [\n" + 
					"    {\"name\": \""+khkParams.getKhkName()+"\"\n" + 
					"    }\n" + 
					"  ],\n" + 
					"  \"localPortRange\": null,\n" + 
					"  \"preferredAuthenticationType\": \""+khkParams.getAuthType()+"\",\n" + 
					"  \"preferredCipher\": null,\n" + 
					"  \"preferredMacAlgorithm\": null,\n" + 
					"  \"profileName\": \""+khkParams.getProfileName()+"\",\n" + 
					"  \"remoteHost\": \""+khkParams.getHost()+"\",\n" + 
					"  \"remotePort\": "+khkParams.getPort()+",\n" + 
					"  \"remoteUser\": \""+khkParams.getUsername()+"\",\n" + 
					"  \"responseTimeOut\": null,\n" + 
					"  \"retryDelay\": null,\n" ;
					if (khkParams.getAuthType().equalsIgnoreCase("password")) {
						outputStr = outputStr + "  \"sshPassword\": \""+khkParams.getPassword()+"\",\n";
						outputStr = outputStr + "  \"userIdentityKey\": null\n";
					} else {
						outputStr = outputStr + "  \"sshPassword\": null,\n";
						outputStr = outputStr + "  \"userIdentityKey\": \""+khkParams.getUserIdentityKey()+"\"\n";
					}
					outputStr += "}\n" ;
			byte[] outputBytes = outputStr.getBytes("UTF-8");
			OutputStream os = con.getOutputStream();
			os.write(outputBytes);

			os.close();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			
			
			return content.toString();
		}
		return null;
	}
	
	public static String GetRemoteSftpProfile(WorkFlowContext wfc,String sftpProfile) throws Exception {
		HashMap<String, String> config = GetConfig();
		if (config != null) {
			String apiUrl = config.get("SFG_API_BASEURL");
			String apiUser = config.get("SFG_API_USERNAME");
			String apiPasswd = config.get("SFG_API_PASSWORD");
			
			String auth = apiUser + ":" + apiPasswd;
			byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
			String authHeaderValue = "Basic " + new String(encodedAuth);
			System.out.println(authHeaderValue);
			
			URL url = new URL(apiUrl + "/B2BAPIs/svc/sshremoteprofiles/"+sftpProfile);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", authHeaderValue);
			con.setConnectTimeout(15000);
			con.setDoOutput(true);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			try {
				wfc.setWFContent("SFTPProfile/Response",content.toString());
				JSONReader reader = new JSONReader();
				HashMap result = (HashMap)reader.read(content.toString());
				ArrayList khkData = (ArrayList)result.get( "knownHostKeys");
				HashMap khkDetails = (HashMap) khkData.get(0);
				String khkName = (String)khkDetails.get("name");
				return khkName;
			}catch(Exception e) {
				StringWriter sw = new StringWriter();
		        e.printStackTrace(new PrintWriter(sw));
				wfc.setWFContent("ExceptionRaised/GetKHK", sw.toString());
			}
			
		}
		return null;
	}
	
	public static String checkKnownHostKey(WorkFlowContext wfc,KhkParameters khkParams) throws Exception {
		HashMap<String, String> config = GetConfig();
		if (config != null) {
			String apiUrl = config.get("SFG_API_BASEURL");
			String apiUser = config.get("SFG_SI_USERNAME");
			String apiPasswd = config.get("SFG_SI_PASSWORD");
			
			String auth = apiUser + ":" + apiPasswd;
			byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
			String authHeaderValue = "Basic " + new String(encodedAuth);
			System.out.println(authHeaderValue);

			URL url = new URL(apiUrl + "/B2BAPIs/svc/sshknownhostkeys/?_include=keyId%2CkeyData&searchFor=");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", authHeaderValue);
			con.setConnectTimeout(15000);
			con.setDoOutput(true);
			
			String[] arr = khkParams.getSshKeyData().split(" ");
			if (arr.length == 3) {
				khkParams.setSshKeyData(arr[1]);
			}
			
			byte[] encoded = Base64.getEncoder().encode(khkParams.getSshKeyData().getBytes(StandardCharsets.UTF_8));
			String encodedStr = new String(encoded);
			
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			Boolean found = false;
			JSONReader reader = new JSONReader();
			ArrayList<HashMap> result = (ArrayList<HashMap>)reader.read(content.toString());
			String khkColl = "";
			for(HashMap data:result) {
				String khkData =(String) data.get("keyData");
				String keyId = (String) data.get("_id");
				
				String decodedStr =  new String(Base64.getDecoder().decode(khkData.getBytes(StandardCharsets.UTF_8)));
				
				decodedStr = decodedStr.replace('\n', ' ');
				String[] arr2 = decodedStr.split(" ");
				if (arr2.length >= 3) {
					decodedStr = arr2[1];
				}
			
				if (decodedStr.equalsIgnoreCase(khkParams.getSshKeyData())) {
					found = true;
					if (khkColl.length()==0) {
						khkColl += "'"+keyId+"'";
					} else {
						khkColl = khkColl+","+ "'"+keyId+"'";
					}
				}
			}
			
			if (found) {
				String query = "select profile_id, name, remote_host, remote_port,khost_key_id from sftp_khost_profiles where khost_key_id in ("+khkColl+")";
				return "<request><query>"+query+"</query>"+"<status>sftp_known_host_key is already present</status></request>";
			} else {
				return "<request><status>sftp_known_host_key not present</status></request>";
			}
		}
		return null;
	}
	
	public static String getKnownHostKeyList(WorkFlowContext wfc) throws Exception {
		HashMap<String, String> config = GetConfig();
		if (config != null) {
			String apiUrl = config.get("SFG_API_BASEURL");
			String apiUser = config.get("SFG_SI_USERNAME");
			String apiPasswd = config.get("SFG_SI_PASSWORD");
			
			String auth = apiUser + ":" + apiPasswd;
			byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
			String authHeaderValue = "Basic " + new String(encodedAuth);
			System.out.println(authHeaderValue);

			URL url = new URL(apiUrl + "/B2BAPIs/svc/sshknownhostkeys/?_include=keyId%2CkeyName&searchFor=");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", authHeaderValue);
			con.setConnectTimeout(15000);
			con.setDoOutput(true);
			
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			
			String output = "";
			
			JSONReader reader = new JSONReader();
			ArrayList<HashMap> result = (ArrayList<HashMap>)reader.read(content.toString());
			
			for(HashMap data:result) {
				String title = (String) data.get("_title");
				String keyId =(String) data.get("keyId");
				String keyName = (String) data.get("keyName");
				output += "<KnownHostKey>\n";
				output +=  "<title>"+title+"</title>\n";
				output += "<keyId>"+keyId+"</keyId>\n";
				output += "<keyName>"+keyName+"</keyName>\n";
				output += "</KnownHostKey>\n";
			}
			
			return "<KnownHostKeys>"+output+"</KnownHostKeys";
		}
		return null;
	}
	
	public static String DeleteKnownHostKey(WorkFlowContext wfc,String knownHostKey) throws Exception {
		HashMap<String, String> config = GetConfig();
		if (config != null) {
			String apiUrl = config.get("SFG_API_BASEURL");
			String apiUser = config.get("SFG_API_USERNAME");
			String apiPasswd = config.get("SFG_API_PASSWORD");
			
			String auth = apiUser + ":" + apiPasswd;
			byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
			String authHeaderValue = "Basic " + new String(encodedAuth);
			System.out.println(authHeaderValue);
			
			URL url = new URL(apiUrl + "/B2BAPIs/svc/sshknownhostkeys/"+knownHostKey);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("DELETE");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", authHeaderValue);
			con.setConnectTimeout(15000);
			con.setDoOutput(true);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			wfc.setWFContent("Profile/DeleteKhkResponse/"+knownHostKey,content.toString());	
			return content.toString();
		}
		wfc.setWFContent("Profile/DeleteKhkError","Failed to delete KHK");	
		return null;
	}
	
	public static String DeleteRemoteSftpProfile(WorkFlowContext wfc,String sftpProfile) throws Exception {
		HashMap<String, String> config = GetConfig();
		if (config != null) {
			String apiUrl = config.get("SFG_API_BASEURL");
			String apiUser = config.get("SFG_API_USERNAME");
			String apiPasswd = config.get("SFG_API_PASSWORD");
			
			String auth = apiUser + ":" + apiPasswd;
			byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
			String authHeaderValue = "Basic " + new String(encodedAuth);
			System.out.println(authHeaderValue);
			
			URL url = new URL(apiUrl + "/B2BAPIs/svc/sshremoteprofiles/"+sftpProfile);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("DELETE");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", authHeaderValue);
			con.setConnectTimeout(15000);
			con.setDoOutput(true);
			
			/*String outputStr = "{}\n" ;
			byte[] outputBytes = outputStr.getBytes("UTF-8");
			OutputStream os = con.getOutputStream();
			os.write(outputBytes);

			os.close();*/
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			wfc.setWFContent("Profile/DeleteSftpProfileResponse/"+sftpProfile,content.toString());
			return content.toString();
		}
		wfc.setWFContent("Profile/DeleteSftpProfileError","Failed to delete Remote Sftp Profile "+sftpProfile);
		return null;
	}
	
	public static String TestSftpConnection(WorkFlowContext wfc,KhkParameters khkParams) throws Exception {
		HashMap<String, String> config = GetConfig();
		if (config != null) {
			String sftpTestUrl = config.get("SFTP_TEST_URL");
			String apiUser = config.get("SFG_SI_USERNAME");
			String apiPasswd = config.get("SFG_SI_PASSWORD");
			
			String auth = apiUser + ":" + apiPasswd;
			byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
			String authHeaderValue = "Basic " + new String(encodedAuth);
			System.out.println(authHeaderValue);
			
			URL url = new URL(sftpTestUrl);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", authHeaderValue);
			con.setConnectTimeout(15000);
			con.setDoOutput(true);
			
			String outputStr = "<Profile>\r\n" + 
					"        <Host>"+khkParams.getHost()+"</Host>\r\n" + 
					"        <Port>"+khkParams.getPort()+"</Port>\r\n" + 
					"        <UserName>"+khkParams.getUsername()+"</UserName>\r\n" + 
					"        <Password>"+khkParams.getPassword()+"</Password>\r\n" + 
					"        <ProfileName>"+khkParams.getProfileName()+"</ProfileName>\r\n" + 
					"        <KHK_NAME>"+khkParams.getKhkName()+"</KHK_NAME>\r\n" + 
					"        <AuthType>"+khkParams.getAuthType()+"</AuthType>\r\n" + 
					"        <UserIdentityKey>"+khkParams.getUserIdentityKey()+"</UserIdentityKey>\r\n" + 
					"        <UserType>"+khkParams.getUserType()+"</UserType>\r\n" + 
					"</Profile>";
			byte[] outputBytes = outputStr.getBytes("UTF-8");
			OutputStream os = con.getOutputStream();
			os.write(outputBytes);

			os.close();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			
			
			return content.toString();
		}
		return null;
	}

	public static class ParameterStringBuilder {
		public static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
			StringBuilder result = new StringBuilder();

			for (Map.Entry<String, String> entry : params.entrySet()) {
				result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				result.append("=");
				result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
				result.append("&");
			}

			String resultString = result.toString();
			return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
		}
	}
	
	
	

}
