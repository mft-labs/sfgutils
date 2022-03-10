package com.mftlabs.sfgutils.bpexec;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.mftlabs.sfgutils.SfgApiClient;
import com.sterlingcommerce.woodstock.workflow.WorkFlowContext;

public class ManageKnownHostKey {

	public static void execute(WorkFlowContext wfc) {
		try {
		
			String host = (String)wfc.getWFContent("Profile/Host");
			String port = (String) wfc.getWFContent("Profile/Port");
			String username = (String) wfc.getWFContent("Profile/UserName");
			String password = (String) wfc.getWFContent("Profile/Password");
			String profileName = (String) wfc.getWFContent("Profile/ProfileName");
			String khkName = (String) wfc.getWFContent("Profile/KHK_NAME");
			String authType = (String) wfc.getWFContent("Profile/AuthType");
			String userIdentityKey = (String) wfc.getWFContent("Profile/UserIdentityKey");
			
			String dataCenter = (String) wfc.getWFContent("Profile/DataCenter");
			
			
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
			khkParams.setUsername(username);
			khkParams.setPassword(password);
			khkParams.setProfileName(dataCenter+"_"+profileName);
			khkParams.setKhkName(dataCenter+"_"+khkName);
			khkParams.setAuthType(authType);
			khkParams.setUserIdentityKey(userIdentityKey);
			khkParams.setSshKeyData(sshKeyData);
			khkParams.setDataCenter(dataCenter);
			
			String result1 = SfgApiClient.addKnownHostKey(wfc,khkParams);
			wfc.setWFContent("addKnownHostKeyResults", result1); 
			
			String result2 = SfgApiClient.addSshRemoteProfile(wfc,khkParams);
			wfc.setWFContent("addSshRemoteProfileResults", result2);
			
		} catch(Exception e) {
			StringWriter sw = new StringWriter();
	         e.printStackTrace(new PrintWriter(sw));
			wfc.setWFContent("ExceptionRaised", sw.toString());
			throw new RuntimeException(e);
		}
	}
}
