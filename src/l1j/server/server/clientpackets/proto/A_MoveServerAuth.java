package l1j.server.server.clientpackets.proto;

import java.util.logging.Logger;

import l1j.server.server.GameClient;
import l1j.server.server.clientpackets.Authorization;
import l1j.server.server.controller.LoginController;
import l1j.server.server.serverpackets.S_MoveServerAuthError;

// 轉移伺服器驗證
public class A_MoveServerAuth extends ProtoHandler {
	private static Logger _log = Logger.getLogger(A_MoveServerAuth.class.getName());
	protected A_MoveServerAuth(){}
	private A_MoveServerAuth(byte[] data, GameClient client) {
		super(data, client);
	}
	
	private int dest_serverno;
	public int get_dest_serverno() {
		return dest_serverno;
	}

	@Override
	protected void doWork() throws Exception {
		readP(1);// 0x08
		dest_serverno = readC();
		
		if (_client.getAccount() != null) {
			_client.accountDisconnect();
		}
		LoginController.getInstance().logout(_client);
		
		String[] loginInfo = _client.getLoginInfo();
		if (loginInfo == null) {
			_client.sendPacket(S_MoveServerAuthError.INVALID_SERVER);
			_client.close();
			return;
		}
		
		String accountName		= _client.getAccountName();
		//String password			= session.getPassword();
		_log.finest(String.format("Request A_MoveServerAuth from user : %s", accountName));
		//Authorization.getInstance().auth(_client, accountName, password, _client.getIp(), _client.getHostname());
	}

	@Override
	protected ProtoHandler copyInstance(byte[] data, GameClient client) {
		return new A_MoveServerAuth(data, client);
	}

}

