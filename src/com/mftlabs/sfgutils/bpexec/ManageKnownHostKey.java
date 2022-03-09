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
			
			
			/*wfc.setWFContent("SSHReceived", sshKeyData);
			byte[] encoded = Base64.getEncoder().encode(sshKeyData.getBytes(StandardCharsets.UTF_8));
			wfc.setWFContent("Base64Encoded", new String(encoded));
			
			System.out.printf("SSH Received:%s",sshKeyData);
			System.out.printf("Base64 Encoded", new String(encoded));*/
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
			
			/*String outputStr = "{"
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
			//System.out.printf("Data sending to Add Remote Profile %v", outputStr);
			wfc.setWFContent("RequestSendingWithPayload", outputStr);*/
			
			
			
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
