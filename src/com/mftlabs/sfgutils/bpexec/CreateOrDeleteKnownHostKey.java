package com.mftlabs.sfgutils.bpexec;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.mftlabs.sfgutils.SfgApiClient;
import com.sterlingcommerce.woodstock.workflow.WorkFlowContext;

public class CreateOrDeleteKnownHostKey {

	//<http-method>POST</http-method>
	/*
	 * <Profile>
		    <Host>192.168.25.250</Host>
		    <Port>22</Port>
		    <UserType>INT</UserType>
		    <KHK_NAME>TEST_KOTI_001</KHK_NAME>
		</Profile>
	 *
	 */
	//<http-method>DELETE</http-method>
	/*
	 * <Profile>
		    <KHK_NAME>TEST_KOTI_001</KHK_NAME>
		</Profile>
	 *
	 */
	public static void execute(WorkFlowContext wfc) {
		try {
			String host = (String)wfc.getWFContent("Profile/Host");
			String port = (String) wfc.getWFContent("Profile/Port");
			String khkName = (String) wfc.getWFContent("Profile/KHK_NAME");
			String httpMethod = (String) wfc.getWFContent("http-method");
			BufferedReader in = new BufferedReader(new InputStreamReader((InputStream)wfc.getPrimaryDocument().getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			
			String sshKeyData = content.toString();
			KhkParameters khkParams = new KhkParameters();
			khkParams.setHost(host);
			khkParams.setPort(port);
			khkParams.setKhkName(khkName);
			khkParams.setSshKeyData(sshKeyData);
			String result1 = SfgApiClient.checkKnownHostKey(wfc,khkParams);
			wfc.setWFContent("createOrDeleteKnownHostKeyResultsForKHK", result1);
			if (httpMethod.equalsIgnoreCase("POST") && result1.indexOf("sftp_known_host_key is already present")!=-1) {
				wfc.setWFContent("createOrDeleteKnownHostKeyResults", result1);
				return;
			}
			
			khkParams.setSshKeyData(sshKeyData);
			if (httpMethod.equalsIgnoreCase("POST")) {
				String result2 = SfgApiClient.addKnownHostKey(wfc,khkParams);
				wfc.setWFContent("createOrDeleteKnownHostKeyResults", result2); 
				return;
			}
			if(httpMethod.equalsIgnoreCase("DELETE")) {
				String result2 = SfgApiClient.DeleteKnownHostKey(wfc,khkName);
				if(result2.indexOf("\"rowsAffected\": 1")!=-1) {
					result2 = "Known Host Key "+khkName+" successfully deleted.";
				}
				wfc.setWFContent("createOrDeleteKnownHostKeyResults", result2);
				return;
			}
			
		} catch(Exception e) {
			StringWriter sw = new StringWriter();
	         e.printStackTrace(new PrintWriter(sw));
			wfc.setWFContent("ExceptionRaised", sw.toString());
			throw new RuntimeException(e);
		}
	}
}
