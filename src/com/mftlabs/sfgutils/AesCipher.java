package com.mftlabs.sfgutils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;

import com.sterlingcommerce.woodstock.workflow.Document;
import com.sterlingcommerce.woodstock.workflow.WorkFlowContext;

public class AesCipher {

	 private byte[] key;
	 private byte[] iv;
	 private byte[] contents;
	 private int bs;

	 private static final String ALGORITHM="AES";
	 
	 public static void execute(WorkFlowContext wfc) {
			try {
					InputStream fileInputStream = (InputStream)wfc.getPrimaryDocument().getInputStream();
					byte[] data = IOUtils.toByteArray(fileInputStream);
					String flpkey = (String)wfc.getWFContent("AMF/flpkey");
					AesCipher cipher = new AesCipher(flpkey.getBytes("UTF-8"));
					byte[] converted = cipher.decrypt(data);
					Document doc = new Document();
				    doc.setBody(converted);
				    wfc.putPrimaryDocument(doc);
			} catch(Exception e) {
				e.printStackTrace();
				wfc.setWFContent("AMF/Status", "Failed");
				wfc.setWFContent("AMF/StatusText", e.getMessage());
				throw new RuntimeException(e);
			}
	 }
	 
	 public AesCipher(byte[] key) {
		 this.bs = 16;
		 //this.key = AesCipher.getSHA256Hash(key);
		 this.key = key;
	 }
	 
	 private void splitData(byte[] data) throws Exception {
		 ByteBuffer bb = ByteBuffer.wrap(data);
		    byte[] ignore = new byte[2];
		    this.iv = new byte[32];
		    this.contents = new byte[data.length-34];
		    bb.get(ignore,0,ignore.length);
		    bb.get(iv, 0, iv.length);
		    bb.get(contents, 0, contents.length);
		    String ivTemp = new String(this.iv);
		    this.iv = DatatypeConverter.parseHexBinary(ivTemp);
		    String contentTemp = new String(this.contents);
		    this.contents = DatatypeConverter.parseHexBinary(contentTemp);
	 }
	 
	 public byte[] decrypt(byte[] encrypted) throws Exception {
		this.splitData(encrypted);
		SecretKeySpec secretKey=new SecretKeySpec(this.key,ALGORITHM);
		//DatatypeConverter.parseHexBinary(new String(this.iv));
        IvParameterSpec ivParameterSpec=new IvParameterSpec(this.iv);
        Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
        this.bs=cipher.getBlockSize();
        cipher.init(Cipher.DECRYPT_MODE,secretKey,ivParameterSpec);
        return cipher.doFinal(this.contents);
	 }
	 
	 public static void writeToFile(String fpath, byte[] contents) throws Exception {
		 FileOutputStream fout = new FileOutputStream(fpath);
		 fout.write(contents);
		 fout.close();
	 }
	 
	 
	 
	 private static byte[] getSHA256Hash(byte[] data) {
	        String result = null;
	        try {
	            MessageDigest digest = MessageDigest.getInstance("SHA-256");
	            byte[] hash = digest.digest(data);
	            //return bytesToHex(hash); 
	            return hash;
	        }catch(Exception ex) {
	            ex.printStackTrace();
	        }
	        return new byte[0];
	  }
	 
	 private static String  bytesToHex(byte[] hash) {
	        return DatatypeConverter.printHexBinary(hash);
	 }
	 
	 private static byte[]  hexToBytes(String hash) {
	        return DatatypeConverter.parseHexBinary(hash);
	 }
	 
	 private static byte[] readContentIntoByteArray(File file)
	 {
	      FileInputStream fileInputStream = null;
	      byte[] bFile = new byte[(int) file.length()];
	      try
	      {
	         //convert file into array of bytes
	         fileInputStream = new FileInputStream(file);
	         fileInputStream.read(bFile);
	         fileInputStream.close();
	      }
	      catch (Exception e)
	      {
	         e.printStackTrace();
	      }
	      return bFile;
	 }
	 
	 public static void main(String[] args) throws Exception {
	     //SfgApiClient.isTradingPartnerGM("SampleMailbox");
		 System.out.println("Reading file contents from:"+args[0]);
		 File file = new File(args[0]);
	     byte[] data = AesCipher.readContentIntoByteArray(file);
	     AesCipher cipher = new AesCipher(args[1].getBytes("UTF-8"));
	     byte[] converted = cipher.decrypt(data);
	     AesCipher.writeToFile(args[2], converted);
	     
	 }
	    
}
