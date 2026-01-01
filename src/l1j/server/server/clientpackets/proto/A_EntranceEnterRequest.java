package l1j.server.server.clientpackets.proto;

import java.util.logging.Logger;

import l1j.server.server.GameClient;
import l1j.server.server.clientpackets.Authorization;
import l1j.server.server.controller.LoginController;
import l1j.server.server.serverpackets.S_MoveServerAuthError;

public class A_EntranceEnterRequest extends ProtoHandler {
	private static Logger _log = Logger.getLogger(A_EntranceEnterRequest.class.getName());
	protected A_EntranceEnterRequest(){}
	private A_EntranceEnterRequest(byte[] data, GameClient client) {
		super(data, client);
	}

	@Override
	protected void doWork() throws Exception {
		if (_client == null) {
			return;
		}
		LoginController.getInstance().logout(_client);
		String[] loginInfo = _client.getLoginInfo(); // 보관중인 로그인 세션 정보
		if (loginInfo == null) {
			_client.sendPacket(S_MoveServerAuthError.INVALID_SERVER);
			_client.kick();
			return;
		}
		
		String accountName		= _client.getAccountName();
		//String password			= _client.getPassword();
		_log.finest("Request entrance enter from user : " + accountName);
		//Authorization.getInstance().auth(_client, accountName, password, _client.getIp(), _client.getHostname());
	}

	@Override
	protected ProtoHandler copyInstance(byte[] data, GameClient client) {
		return new A_EntranceEnterRequest(data, client);
	}
}

