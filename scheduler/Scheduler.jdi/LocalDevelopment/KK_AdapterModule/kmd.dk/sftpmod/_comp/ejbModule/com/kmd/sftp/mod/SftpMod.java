package com.kmd.sftp.mod;

import javax.ejb.Stateless;

import com.sap.aii.af.lib.mp.module.Module;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.lib.mp.module.ModuleException;
import com.sap.aii.af.lib.mp.module.ModuleHome;
import com.sap.aii.af.lib.mp.module.ModuleLocal;
import com.sap.aii.af.lib.mp.module.ModuleLocalHome;
import com.sap.aii.af.lib.mp.module.ModuleRemote;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.Payload;
import com.sap.engine.interfaces.messaging.api.PublicAPIAccessFactory;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditAccess;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;
import javax.ejb.Local;
import javax.ejb.Remote;

/**
 * Session Bean implementation class SftpMod
 */
@Stateless(name = "SftpMod")
@Local(value = { ModuleLocal.class })
@Remote(value = { ModuleRemote.class })
@LocalHome(value=ModuleLocalHome.class)
@RemoteHome(value=ModuleHome.class)
public class SftpMod implements Module {

    /**
     * Default constructor. 
     */
    public SftpMod() {
        // TODO Auto-generated constructor stub
    }

	@Override
	public ModuleData process(ModuleContext mContext, ModuleData mData)
			throws ModuleException {
		// TODO Auto-generated method stub
		Object obj = null;
		Message msg = null;
		MessageKey key = null;
		Payload pload = null;
		String pdesc = null;
		String pcontenttype = null;
		String fullpload = null;
		
		try{
			obj = mData.getPrincipalData();
			msg = (Message)obj;
			key = new MessageKey(msg.getMessageId(),msg.getMessageDirection());
			//To get the message payload
			pload = msg.getMainPayload();
			pdesc = pload.getDescription();
			
			//get the payload as Byte[]
			fullpload = new String (pload.getContent());
			
			AuditAccess audit = PublicAPIAccessFactory.getPublicAPIAccess().getAuditAccess();
			
			audit.addAuditLogEntry(msg.getMessageKey(), AuditLogStatus.SUCCESS, "SftpMod: Read Complete Payload");
			String FilePath = (String) mContext.getContextData("fileDirectory");
			audit.addAuditLogEntry(msg.getMessageKey(), AuditLogStatus.SUCCESS, "SftpMod: File Path is :"+FilePath);
			String host = (String) mContext.getContextData("host");
			audit.addAuditLogEntry(msg.getMessageKey(), AuditLogStatus.SUCCESS, "SftpMod: Host is :"+host);
			String port = (String) mContext.getContextData("port");
			audit.addAuditLogEntry(msg.getMessageKey(), AuditLogStatus.SUCCESS, "SftpMod: port is :"+port);
			String userName = (String) mContext.getContextData("userName");
			audit.addAuditLogEntry(msg.getMessageKey(), AuditLogStatus.SUCCESS, "SftpMod: Username is :"+userName);
			
			
		}catch(Exception e){
			ModuleException me = new ModuleException(e);
		      throw me;
		}
		return mData;
	}

}
