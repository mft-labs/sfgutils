package com.mftlabs.sfgutils.bpexec;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.mftlabs.sfgutils.SfgApiClient;
import com.sterlingcommerce.woodstock.workflow.WorkFlowContext;

public class DeleteRemoteSftpProfile {
	public static void execute(WorkFlowContext wfc) throws Exception {
		try {
			String profileName = (String)wfc.getWFContent("Details/ProfileName");
			String khkName =  SfgApiClient.GetRemoteSftpProfile(wfc,profileName);
			if (khkName != null) {
				try {
					SfgApiClient.DeleteRemoteSftpProfile(wfc,profileName);
					try {
						SfgApiClient.DeleteKnownHostKey(wfc, khkName);
					} catch(Exception e2) {
						StringWriter sw = new StringWriter();
				         e2.printStackTrace(new PrintWriter(sw));
						wfc.setWFContent("ExceptionRaised/DeleteKhk", sw.toString());
						throw new RuntimeException(e2);
					}
					
				} catch(Exception e1) {
					StringWriter sw = new StringWriter();
			         e1.printStackTrace(new PrintWriter(sw));
					wfc.setWFContent("ExceptionRaised/DeleteSftpProfile", sw.toString());
					throw new RuntimeException(e1);
				}
				
				wfc.setWFContent("Profile/Status", "Successfully deleted "+profileName+" and "+khkName);
			} else {
				wfc.setWFContent("Profile/Error",profileName+" not found, please verify" );	
			}
			
		} catch(Exception e) {
			StringWriter sw = new StringWriter();
	         e.printStackTrace(new PrintWriter(sw));
			wfc.setWFContent("ExceptionRaised/ErrorDetails", sw.toString());
			throw new RuntimeException(e);
		}
		
		
	}
}
