package l1j.server.server.clientpackets.proto;

import java.util.logging.Logger;

import l1j.server.server.GameClient;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.clientpackets.Authorization;
import l1j.server.server.datatables.BanAccountTable;
import l1j.server.server.datatables.BanAccountTable.BanAccount;
import l1j.server.server.serverpackets.message.S_CommonNews;
import l1j.server.server.utils.CharsetUtil;
import l1j.server.server.utils.DelayClose;

public class A_NpLogin extends ProtoHandler {
	private static Logger _log = Logger.getLogger(A_NpLogin.class.getName());
	protected A_NpLogin(){}
	private A_NpLogin(byte[] data, GameClient client) {
		super(data, client);
	}

	@Override
	protected void doWork() throws Exception {
		readP(2);		// total length
		
		readP(1);		// 0x08
		readBit();		// IP

		readP(1);		// 0x10
		readBit();		// OTP

		readP(1);		// 0x1A
		int sublength = readC();
		readP(sublength);// AUTH NP

		readP(1);		// 0x22
		int account_length = readBit(); // account length
		byte[] account_bytes = readByte(account_length);

		readP(1);		// 0x2A
		int maclength = readC();
		readP(maclength);// MAC_HASH

		String[] authToken = null;
		try {
			authToken = new String(account_bytes, CharsetUtil.BIG5_STR).split("\\n"); // 用換行符號分隔帳號和密碼
		} catch(Exception e) {
			System.out.println(String.format("[A_NpLogin] Authorization token decryption failed: IP(%s)\r\nINFO =>\r\n%s", _client.getIp(), toString()));
			_client.sendPacket(S_CommonNews.OTHER_CONNECTOR);
			GeneralThreadPool.getInstance().schedule(new DelayClose(_client), 1500L);
			return;
		}

		String accountName = authToken[0].toLowerCase();
		String password = authToken[1].toLowerCase();
		
		// Check Ban Account
		if (authToken.length > 2 && hddBanCheck(accountName)) return;

		_log.finest("Request NP Login from user : " + accountName);
		Authorization.getInstance().auth(_client, accountName, password, _client.getIp(), _client.getHostname());
		_client.setLoginInfo(authToken); // 設置登入帳號
		if(_client.getAccount() != null) _client.getAccount().setLoginInfo(authToken);
		_client.loginInfoToken = account_bytes;
	}

	private boolean hddBanCheck(String accountName){
		BanAccount ban = BanAccountTable.getBan(accountName);
		if (ban != null) {
			_client.sendPacket(S_CommonNews.HDD_BAN_CHECK);
			System.out.println("\n┌───────────────────────────────┐");
			System.out.println("\t Hardban access blocked! Account = " + accountName + ", IP = " + _client.getIp());
			System.out.println("└───────────────────────────────┘\n");
			GeneralThreadPool.getInstance().schedule(new DelayClose(_client), 500L);
			return true;
		}
		return false;
	}

	@Override
	protected ProtoHandler copyInstance(byte[] data, GameClient client) {
		return new A_NpLogin(data, client);
	}

}

