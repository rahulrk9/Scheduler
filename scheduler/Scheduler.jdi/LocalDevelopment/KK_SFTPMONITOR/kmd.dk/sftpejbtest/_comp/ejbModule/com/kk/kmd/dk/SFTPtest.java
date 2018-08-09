package com.kk.kmd.dk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.MessageListener;
import javax.naming.InitialContext;

import org.bouncycastle.openssl.PEMWriter;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import java.util.logging.Logger;
import com.jcraft.jsch.Session;
import com.sap.aii.af.service.resource.SAPSecurityResources;
import com.sap.aii.security.lib.KeyStoreManager;
import com.sap.aii.security.lib.PermissionMode;
import com.sap.engine.interfaces.keystore.KeystoreManager;

import com.sap.scheduler.runtime.JobContext;
import com.sap.scheduler.runtime.JobParameter;
import com.sap.scheduler.runtime.mdb.MDBJobImplementation;
import com.sap.security.api.ssf.ISsfProfile;
import com.sap.security.core.server.ssf.SsfProfileKeyStore;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;




/**
 * Message-Driven Bean implementation class for: SFTPtest
 *
 */
@MessageDriven(
		activationConfig = { @ActivationConfigProperty(
                propertyName="messageSelector",
                propertyValue="JobDefinition='SFTPTestJob'"),
                @ActivationConfigProperty(
				propertyName = "destinationType", propertyValue = "javax.jms.Queue"
		) })
public class SFTPtest extends MDBJobImplementation implements MessageListener {
       
    /**
     * @see MDBJobImplementation#MDBJobImplementation()
     */
    public SFTPtest() {
        super();
        // TODO Auto-generated constructor stub
    }
	


	@Override
	public void onJob(JobContext ctx) throws Exception {
		// TODO Auto-generated method stub
		//Parameters - keystore_name, key_alias, user,host,port
		Logger logger = ctx.getLogger();
		logger.info("Inside onJob");
		String host= "";
		boolean success = false;
		String systemID = "";
		String to ="";
		String from="";
		String port = "";
		String keyStore ="";
		String keyAlias="";
		String user ="";
		try{
			

			
			logger.info("SFTP Check Job Started ");
			
			JobParameter mailBoxParameter = ctx.getJobParameter("Mailbox");
			String mailbox = mailBoxParameter.getStringValue();
			logger.info("mailBox provided :"+mailbox);

			JobParameter fromParameter = ctx.getJobParameter("From");
			from = fromParameter.getStringValue();
			logger.info("mailBox provided :"+from);
			
			JobParameter toParameter = ctx.getJobParameter("To");
			to = toParameter.getStringValue();
			logger.info("mailBox provided :"+to);
			
			
			JobParameter userParameter = ctx.getJobParameter("Username");
			user = userParameter.getStringValue();
			logger.info("Username provided :"+user);

			JobParameter hostParameter = ctx.getJobParameter("Host");
			 host = hostParameter.getStringValue();
			logger.info("Host provided :"+host);

			JobParameter portParameter = ctx.getJobParameter("Port");
			port = portParameter.getStringValue();
			logger.info("Port provided :"+port);
			
			
			//SFTP
			JobParameter keyStoreNameParameter = ctx.getJobParameter("KEYSTORE_NAME");
			keyStore = keyStoreNameParameter.getStringValue();
			logger.info("KEYSTORE_NAME provided :"+keyStore);

			JobParameter keyAliasParameter = ctx.getJobParameter("KEY_ALIAS");
			keyAlias = keyAliasParameter.getStringValue();
			logger.info("KEY_ALIAS provided :"+keyAlias);
			
			//Server Name
			systemID = System.getProperty("SAPSYSTEMNAME");
			logger.info("SFTP is called from system :"+systemID);
//			if(!SFTPTest(user,host,port,keyStore,keyAlias)){
//
//				logger.info("SFTP can't be conected");
//			}else{
//				logger.info("SFTP can be conected");
//			}
			logger.info("Connecting to SFTP");
			
			try{
				SsfProfileKeyStore profile = null;
				
				Session session = null;
				String res = null;
			//SFTPTest(user,host,port,keyStore,keyAlias);
				//SAPSecurityResources secRes = SAPSecurityResources.getInstance();
				InitialContext ic = new InitialContext();
				
				logger.info("Getting instance secres");
              //  KeyStoreManager manager =
               //           secRes.getKeyStoreManager(PermissionMode.SYSTEM_LEVEL);
				KeystoreManager manager = (KeystoreManager) ic.lookup("keystore");
                logger.info("Getting manager");
                KeyStore keyStore1 = manager.getKeystore(keyStore.trim());//SFTP
                logger.info("Getting keystore1");
                profile = new SsfProfileKeyStore(keyStore1,keyAlias.trim(), null);
                logger.info("Getting profile");
                X509Certificate cert = (X509Certificate) keyStore1.getCertificate(keyAlias.trim());//"KK_GOANYWHERE_A_DEV_SFTP"
                cert.checkValidity();
                logger.info("Check Validity");
               
                PrivateKey privKey = profile.getPrivateKey();
                logger.info("After priv key");
				StringWriter stringWriter = new StringWriter();
				logger.info("After StringWriter");
				PEMWriter pemWriter = new PEMWriter(stringWriter);
				pemWriter.writeObject(privKey);
				pemWriter.close();
				logger.info("After PEMWriter");
				res = stringWriter.toString();

				File file_pk = new File("pk_tmp_BG_vers.ppk");
		          FileOutputStream fileOuputStream = new FileOutputStream(file_pk);
		          fileOuputStream.write(res.getBytes());
		          fileOuputStream.close();
		          JSch jsch = new JSch();
		          jsch.addIdentity("pk_tmp_BG_vers.ppk");

				session = jsch.getSession(user, host, Integer.parseInt(port));
				
				java.util.Properties config = new java.util.Properties();
				config.put("StrictHostKeyChecking", "no");
				
				session.setConfig(config);
				session.connect();
				logger.info("Session Connected :"+session.isConnected());
				logger.info("Session test : "+session.isConnected());
			
				if(session.isConnected()){
					success = true;
				}
				com.jcraft.jsch.Channel channel = session.openChannel("sftp");
				channel.connect();
				//change for file write
				/*ChannelSftp sftpChannel = (ChannelSftp) channel;
				sftpChannel.put(src, dst, mode);*/
				//change for file write
				logger.info("Channel Connected :"+channel.isConnected());
				
				channel.disconnect();
				session.disconnect();
				
			}catch(Exception e){
				logger.info("Exception occured: "+e.getLocalizedMessage());	
				for(int i=0 ; i<e.getStackTrace().length;i++){
					logger.info(e.getStackTrace()[i].toString());
				}
				//postMail(host,systemID);
			}
			
			//SFTPTest(user, host, port, keyStore, keyAlias);
		}catch(Exception e){
			logger.info("Exception occured: "+e.getLocalizedMessage());
			logger.info("Exception occured: "+e.getMessage());
		}
		
		if(!success){
			postMail(host,systemID,to,from,port,keyStore,keyAlias,user);
		}
	}
	
//	private void SFTPTest(String user,String host,String port,String keystore,String keyAlias)  {
//		//ISsfProfile profile;
//		
//		String res = null;
//		String KEYSTOREMANAGER_JNDI_NAME = "keystore";
//		String KEYSTORE_NAME = keystore;
//		String KEY_ALIAS = keyAlias; 
//		int portNumber = Integer.parseInt(port);
//		
//		SsfProfileKeyStore profile = null;
//
//
//	}
	
	private void postMail(String host, String server, String to, String from,String port, String keyStore, String keyAlias, String user){
		try{
			String mailhst = "10.2.1.41";
			
			String subject = "Alert from PO regarding failure of SFTP: "+host;
			
			String message = "\nFrom system:  "+server+"\nPO have problems with SFTP connection to: "+host+":"+port+"\nKeystore : "+keyStore+"\nKeyAlias : "+keyAlias+"\nUsername : "+user;
		boolean debug = false;
		java.util.Properties props = new java.util.Properties();
		props.put("mail.smtp.host", mailhst);
		javax.mail.Session session = javax.mail.Session.getDefaultInstance(props, null);
		session.setDebug(debug);
		Message msg = new MimeMessage(session);
		InternetAddress addressFrom = new InternetAddress(from);
		msg.setFrom(addressFrom);
		String recipients[] = to.split(";");
		InternetAddress[] addressTo = new InternetAddress[recipients.length];
		for (int i = 0; i < recipients.length; i++)
			addressTo[i] = new InternetAddress(recipients[i]);
		msg.setRecipients(Message.RecipientType.TO, addressTo);
		msg.addHeader("MyHeaderName", "myHeaderValue");
		msg.setSubject(subject);
		msg.setContent(message, "text/plain");
		Transport.send(msg);
		}catch(Exception e){
			
		}
	}
	

}
