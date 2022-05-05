package com.mftlabs.sfgutils.bpexec;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.mftlabs.sfgutils.SfgApiClient;
import com.sterlingcommerce.woodstock.workflow.WorkFlowContext;

public class CheckKnownHostKey {
	public static void execute(WorkFlowContext wfc) {
		try {
		
			String host = (String)wfc.getWFContent("Profile/Host");
			String port = (String) wfc.getWFContent("Profile/Port");
			
			
			BufferedReader in = new BufferedReader(new InputStreamReader((InputStream)wfc.getPrimaryDocument().getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			String sshKeyData = content.toString() ;
			/*String[] arr = content.toString().split(" ");
			if (arr.length == 3) {
				sshKeyData = arr[1];
			} else if (arr.length == 1) {
				sshKeyData= content.toString();
			}*/
			
			KhkParameters khkParams = new KhkParameters();
			khkParams.setHost(host);
			khkParams.setPort(port);
			khkParams.setSshKeyData(sshKeyData);

			
			String result1 = SfgApiClient.checkKnownHostKey(wfc,khkParams);
			wfc.setWFContent("checkKnownHostKeyResults", result1); 
			
		} catch(Exception e) {
			StringWriter sw = new StringWriter();
	         e.printStackTrace(new PrintWriter(sw));
			wfc.setWFContent("ExceptionRaised", sw.toString());
			throw new RuntimeException(e);
		}
	}
}
