package com.mftlabs.sfgutils.bpexec;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.mftlabs.sfgutils.SfgApiClient;
import com.sterlingcommerce.woodstock.workflow.WorkFlowContext;

public class CreateOrDeleteRemoteSftpProfile {
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
			String httpMethod = (String) wfc.getWFContent("http-method");
			
			KhkParameters khkParams = new KhkParameters();
			khkParams.setHost(host);
			khkParams.setPort(port);
			khkParams.setUsername(username);
			khkParams.setPassword(password);
			khkParams.setProfileName(profileName);
			khkParams.setKhkName(khkName);
			khkParams.setAuthType(authType);
			khkParams.setUserIdentityKey(userIdentityKey);
			
			if(httpMethod.equalsIgnoreCase("POST")) {
				String result2 = SfgApiClient.addSshRemoteProfile(wfc,khkParams);
				wfc.setWFContent("CreateOrDeleteRemoteSftpProfileResults", result2);
				return;
			}
			if(httpMethod.equalsIgnoreCase("DELETE")) {
				//DeleteRemoteSftpProfile.execute(wfc);
				try {
					//String khkName2 =  SfgApiClient.GetRemoteSftpProfile(wfc,profileName);
					//if (khkName2 != null) {
						SfgApiClient.DeleteRemoteSftpProfile(wfc,profileName);
						wfc.setWFContent("Profile/Status", "Successfully deleted "+profileName);
					//}
				} catch(Exception e2) {
					StringWriter sw = new StringWriter();
			         e2.printStackTrace(new PrintWriter(sw));
					wfc.setWFContent("ExceptionRaised", sw.toString());
					throw new RuntimeException(e2);
				}
				
			}
			
		} catch(Exception e) {
			StringWriter sw = new StringWriter();
	         e.printStackTrace(new PrintWriter(sw));
			wfc.setWFContent("ExceptionRaised", sw.toString());
			throw new RuntimeException(e);
		}
	}
}
