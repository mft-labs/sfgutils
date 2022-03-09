package com.mftlabs.sfgutils.bpexec;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.mftlabs.sfgutils.SfgApiClient;
import com.sterlingcommerce.woodstock.workflow.WorkFlowContext;

public class TestSftpRemoteProfile {
	public static void execute(WorkFlowContext wfc) throws Exception {
		String host = (String)wfc.getWFContent("Profile/Host");
		String port = (String) wfc.getWFContent("Profile/Port");
		String username = (String) wfc.getWFContent("Profile/UserName");
		String password = (String) wfc.getWFContent("Profile/Password");
		String profileName = (String) wfc.getWFContent("Profile/ProfileName");
		String khkName = (String) wfc.getWFContent("Profile/KHK_NAME");
		String authType = (String) wfc.getWFContent("Profile/AuthType");
		String userIdentityKey = (String) wfc.getWFContent("Profile/UserIdentityKey");
		String userType = (String) wfc.getWFContent("Profile/UserType");
		
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
		khkParams.setProfileName(profileName);
		khkParams.setKhkName(khkName);
		khkParams.setAuthType(authType);
		khkParams.setUserIdentityKey(userIdentityKey);
		khkParams.setUserType(userType);
		khkParams.setSshKeyData(sshKeyData);
		
		try {
			String result1 = SfgApiClient.addKnownHostKey(wfc,khkParams);
			wfc.setWFContent("addKnownHostKeyResults", result1); 
			
			String result2 = SfgApiClient.addSshRemoteProfile(wfc,khkParams);
			wfc.setWFContent("addSshRemoteProfileResults", result2);
			
			//wfc.setWFContent("Result/ProfileCreated", khkParams.getProfileName());
			//wfc.setWFContent("Result/ProfileCreationStatus","Success");
			wfc.setWFContent("Status/ProfileCreation","Success");
		} catch(Exception e1) {
			//wfc.setWFContent("Result/ProfileCreationStatus","Failed");
			wfc.setWFContent("Status/ProfileCreation","Failed");
			StringWriter sw = new StringWriter();
	         e1.printStackTrace(new PrintWriter(sw));
			wfc.setWFContent("ExceptionRaised", sw.toString());
			//throw new RuntimeException(e1);
		}
		
		try {
			String result = SfgApiClient.TestSftpConnection(wfc, khkParams);
			//wfc.setWFContent("Result/RemoteSftpConnectionResponse",result);
			//wfc.setWFContent("Result/RemoteSftpConnectionStatus","Success");
			wfc.setWFContent("Status/ProfileConnection","Success");
		} catch(Exception e2) {
			//wfc.setWFContent("Result/RemoteSftpConnectionStatus","Failed");
			wfc.setWFContent("Status/ProfileConnection","Failed");
			StringWriter sw = new StringWriter();
	         e2.printStackTrace(new PrintWriter(sw));
			wfc.setWFContent("ExceptionRaised", sw.toString());
			//throw new RuntimeException(e2);
		}
		
		try {
			wfc.setWFContent("Details/ProfileName",profileName);
			DeleteRemoteSftpProfile.execute(wfc);
			//wfc.setWFContent("Result/DeleteRemoteSftpProfileStatus","Success");
			wfc.setWFContent("Status/ProfileDeletion","Success");
		} catch(Exception e3) {
			wfc.setWFContent("Status/ProfileDeletion","Failed");
			//wfc.setWFContent("Result/DeleteRemoteSftpProfileStatus","Failed");
			StringWriter sw = new StringWriter();
	         e3.printStackTrace(new PrintWriter(sw));
			wfc.setWFContent("ExceptionRaised", sw.toString());
			//throw new RuntimeException(e3);
		}
		
		
	}
}
