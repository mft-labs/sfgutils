package com.mftlabs.sfgutils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import com.sterlingcommerce.woodstock.workflow.WorkFlowContext;

public class IsTradingPartnerGM {
	
	public static void execute(WorkFlowContext wfc) {
		try {
		
			String partner = (String)wfc.getWFContent("Cigna/Adapter/PARM1");
			 if (partner.split("/").length==1) {
				 partner = (String)wfc.getWFContent("Cigna/Adapter/PARM2");
			 }			 
			 String[] arr = partner.split("/");
			 IsTradingPartnerGM utils = new IsTradingPartnerGM();
			 if (utils.isTradingPartnerGM(arr[1])) {
				 wfc.setWFContent("globalMailboxParter", "true"); 
			 } else {
				 wfc.setWFContent("globalMailboxParter", "false");
			 }
		} catch(Exception e) {
			 StringWriter sw = new StringWriter();
	         e.printStackTrace(new PrintWriter(sw));
			wfc.setWFContent("ExceptionRaised", sw.toString());
			throw new RuntimeException(e);
		}
	}
	
	public boolean isTradingPartnerGM(String partner) throws Exception {		
		//return SfgApiClient.isTradingPartnerGM(partner);
		HashMap<String,Object> result = SfgApiClient.isGMUser(partner);
		return (Boolean)result.get("GMUser");
	}

}
