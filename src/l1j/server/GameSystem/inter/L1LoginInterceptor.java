package l1j.server.GameSystem.inter;

import l1j.server.server.Account;
import l1j.server.server.AccountAlreadyLoginException;
import l1j.server.server.GameClient;
import l1j.server.server.GameServerFullException;
import l1j.server.server.clientpackets.C_LoginToServer;
import l1j.server.server.construct.message.L1SystemMessage;
import l1j.server.server.controller.LoginController;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_LoginResult;
import l1j.server.server.serverpackets.inter.S_HibreedAuth;

public class L1LoginInterceptor implements Runnable {
	private static final int MAX_WAIT_COUNT	= 5;
	private static final long INTERVAL		= 1000L;

	private final GameClient			_client;
	private final L1InterServerModel	_model;
	private final Account				_account;
	private final LoginController		_login;
	private int waitCount;
	
	public L1LoginInterceptor(GameClient client, L1InterServerModel model) {
		_client 	= client;
		_model		= model;
		_account	= model.getAccount();
		_login		= LoginController.getInstance();
	}
	
	@Override
	public void run() {
		try {
			while (_client != null) {
				if (waitCount++ >= MAX_WAIT_COUNT) {
					rollBack();
					break;
				}
				if (!_login.isClientLogin(_account.getName())) {
					enter();
					break;
				}
				Thread.sleep(INTERVAL);
				System.out.println(String.format(
						"[L1LoginInterceptor] Retry connection delay: account (%s), character (%s), number of attempts (%d/%d)",
						_account.getName(), _model.getCharName(), waitCount, MAX_WAIT_COUNT));
			}
		} catch (Exception e) {
			System.out.println(String.format("[L1LoginInterceptor] Exception occurred: IP (%s) was forcibly disconnected.", _client.getIp()));
			e.printStackTrace();
			_client.close();
		}
	}
	
	void enter() {
		try {
			_client.setInterServer(true);
			_client.setInter(_model.getInter());
			_client.setAccount(_account);
			//_client.setLoginSession(_account.getLoginSession());
			_client.getAccount().setValid(true);
			_login.login(_client, _account);// 로그인
			_client.sendPacket(S_HibreedAuth.SUCCESS);
			_client.sendPacket(S_LoginResult.LOGIN_OK);
			C_LoginToServer login = new C_LoginToServer(_client, _model);
			L1InterServerFactory.remove(_model.getCharName());
			if (login != null) {
				login = null;
			}
		} catch (IllegalArgumentException e) {
			System.out.println(String.format("[L1LoginInterceptor] NOT_AUTH : IP(%s)", _client.getIp()));
			_client.close();
		} catch(GameServerFullException e){
			System.out.println(String.format("[L1LoginInterceptor] GAME_SERVER_FULL : IP(%s)", _client.getIp()));
			_client.close();
		} catch (AccountAlreadyLoginException e) {
			System.out.println(String.format("[L1LoginInterceptor] ACCOUNT_ALREADY_LOGIN: IP(%s)", _client.getIp()));
			_client.close();
		} catch (Exception e) {
			System.out.println(String.format("[L1LoginInterceptor] ENTER_EXCEPTION : IP(%s)", _client.getIp()));
			e.printStackTrace();
			_client.close();
		}
	}
	
	void rollBack(){
		try {
			_client.setAccount(_account);
			//_client.setLoginSession(_account.getLoginSession());
			if (_login.isClientLogin(_account.getName())) {
				_login.logout(_client);
			}
			_client.getAccount().setValid(true);
			_login.login(_client, _client.getAccount());// 로그인
			
			_model.setInterX(_model.getRollbackX());
			_model.setInterY(_model.getRollbackY());
			_model.setInterMapId(_model.getRollbackMapId());
			_client.sendPacket(S_LoginResult.LOGIN_OK);
			C_LoginToServer login	= new C_LoginToServer(_client, _model);// 롤백 로그인
			L1PcInstance pc			= _client.getActiveChar();
			if (pc != null) {
				pc.sendPackets(L1SystemMessage.INTER_SERVER_CONNECT_FAIL);
			}
			L1InterServerFactory.remove(_model.getCharName());
			if (login != null) {
				login = null;
			}
			System.out.println(String.format(
					"[Interserver] Entry failed due to connection delay: Character name (%s), delay time (%d seconds) were rolled back.",
					_model.getCharName(), waitCount));
		} catch (IllegalArgumentException e) {
			System.out.println(String.format("[L1LoginInterceptor] ROLLBACK_NOT_AUTH : IP(%s)", _client.getIp()));
			_client.close();
		} catch(GameServerFullException e){
			System.out.println(String.format("[L1LoginInterceptor] ROLLBACK_GAME_SERVER_FULL : IP(%s)", _client.getIp()));
			_client.close();
		} catch (AccountAlreadyLoginException e) {
			System.out.println(String.format("[L1LoginInterceptor] ROLLBACK_ACCOUNT_ALREADY_LOGIN : IP(%s)", _client.getIp()));
			_client.close();
		} catch (Exception e) {
			System.out.println(String.format("[L1LoginInterceptor] ROLLBACK_EXCEPTION : IP(%s)", _client.getIp()));
			e.printStackTrace();
			_client.close();
		}
	}
}

