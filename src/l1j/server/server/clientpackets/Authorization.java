package l1j.server.server.clientpackets;

import java.io.IOException;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.AuthIP;
import l1j.server.server.Account;
import l1j.server.server.AccountAlreadyLoginException;
import l1j.server.server.GameClient;
import l1j.server.server.GameServerFullException;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.controller.LoginController;
import l1j.server.server.controller.action.EntranceQueue;
import l1j.server.server.datatables.IpTable;
import l1j.server.server.serverpackets.S_LoginResult;
import l1j.server.server.serverpackets.message.S_CommonNews;
//import l1j.server.server.utils.CommonUtil;
import l1j.server.server.utils.DelayClose;

public class Authorization {
	private static Authorization uniqueInstance = null;
	private static Logger _log = Logger.getLogger(Authorization.class.getName());
	
	public static Authorization getInstance() {
		if (uniqueInstance == null) {
			synchronized(Authorization.class) {
				if (uniqueInstance == null) {
					uniqueInstance = new Authorization();
				}
			}
		}
		return uniqueInstance;
	}
	
	public synchronized void auth(final GameClient client, String accountName, String password, String ip, String host) throws IOException {
		if (IpTable.isBannedIp(ip)) {
			System.out.println("\n┌───────────────────────────────┐");
			System.out.println("\t Block IP access! Account=" + accountName + " IP=" + ip);
			System.out.println("└───────────────────────────────┘\n");
			client.sendPacket(S_CommonNews.IP_BAN_CHECK);
			GeneralThreadPool.getInstance().schedule(new DelayClose(client), 1500L);
			return;
		}
		if (Config.SERVER.IP_PROTECT && !AuthIP.isWhiteIp(ip)) {
			System.out.println("\n┌───────────────────────────────┐");
			System.out.println("\t Block VPN IP access! Account=" + accountName + " IP=" + ip);
			System.out.println("└───────────────────────────────┘\n");
			client.sendPacket(S_CommonNews.IP_VPN_CHECK);
			GeneralThreadPool.getInstance().schedule(new DelayClose(client), 1500L);
			return;
		}
		
		LoginController login = LoginController.getInstance();
		int loginCount = login.getIpCount(ip);
		if (!Config.SERVER.ALLOW_2PC && loginCount > 0) {
			_log.info("Login denied for two PCs connected with same IP. account=" + accountName + " ip=" + ip);
			client.sendPacket(S_CommonNews.IP_CHECK_FAIL);
			GeneralThreadPool.getInstance().schedule(new DelayClose(client), 1500L);
			return;
		} else if (loginCount >= Config.SERVER.ALLOW_2PC_IP_COUNT) {
			_log.info("The number of connections from the same IP exceeded. account=" + accountName + " ip=" + ip);
			client.sendPacket(S_CommonNews.IP_COUNT_MAX);
			GeneralThreadPool.getInstance().schedule(new DelayClose(client), 1500L);
			return;
		}

		Account account		= Account.load(accountName);
		// 建立帳號IP限制
		if (account == null) {
			if (Config.SERVER.AUTO_CREATE_ACCOUNTS) {
				if (Account.checkLoginIP(ip)) {
					_log.info("已超過帳號建立時間] " + ip);
					client.sendPacket(new S_CommonNews("\n\n每個IP只限建立「 + Config.CREATE_IP_ACCOUNT_COUNT + 」個帳號。\n\n您的IP超過建立數量。\n\n請從其他IP建立帳號或詢問管理員。"));
					GeneralThreadPool.getInstance().schedule(new DelayClose(client), 1500L);
					return;
				} else {
					if (!isValidAccount(accountName)) {
						client.sendPacket(S_LoginResult.ACCOUNT_NAME_FAIL);
						return;
					}
					if (!isValidPassword(password)) {
						client.sendPacket(S_LoginResult.ACCOUNT_PWD_FAIL);
						return;
					}
					
					Account.create(accountName, password, ip, host);
					account = Account.load(accountName);
				}
			} else {
				_log.warning("account missing for user " + accountName);
			}
		}

		if (account == null || !account.validatePassword(password)) {
			client.sendPacket(S_LoginResult.ACCOUNT_FAIL);
			return;
		}

		if (account.isBanned()) { // BAN Account
			_log.info("Login for seized account denied. account=" + accountName + " ip=" + ip);
			//client.sendPacket(new S_LoginResult(S_LoginResult.REASON_BUG_WRONG));
			client.sendPacket(S_CommonNews.ACCOUNT_BAN_CHECK);
			GeneralThreadPool.getInstance().schedule(new DelayClose(client), 1500L);
			return;
		}
		
		// 開發模式下僅GM帳號可存取
		if (Config.SERVER.CONNECT_DEVELOP_LOCK && !account.isGameMaster()) {
			client.sendPacket(S_CommonNews.CONNECT_LOCK);
			GeneralThreadPool.getInstance().schedule(new DelayClose(client), 1500L);
			return;
		}
		
		if (Config.SERVER.ACCESS_STANBY && !EntranceQueue.getInstance().isStanby(client, account)) {
			return;
		}
		
		/*if(account.getAccessLevel() == Config.GMCODE){
			ip = Integer.toString(CommonUtil.random(80) + 100) + StringUtil.PeriodString + Integer.toString(CommonUtil.random(100) + 50) + StringUtil.PeriodString
					+ Integer.toString(CommonUtil.random(100) + 50) + StringUtil.PeriodString + Integer.toString(CommonUtil.random(100) + 50);
			account.setIp(ip);
		}*/

		try {
			login.login(client, account);
			account.addPSSTime();
			account.updateLastActive(ip); // 更新上次登入日期
			client.setAccount(account);
			client.setEnterReady(true);
			entered(client);
		} catch (GameServerFullException e) { // 已連線用戶數超出
			client.sendPacket(S_CommonNews.MAX_USER);
			_log.info("--- Number of connected users exceeded: (" + client.getIp() + ") Login cut off. ");
			client.kick();
			return;
		} catch (AccountAlreadyLoginException e) {		
			_log.info("Connection with the same ID: (" + client.getIp() + ") was forcibly disconnected. ");
			client.sendPacket(S_CommonNews.RE_LOGIN);
			client.kick();
			return;
		} catch (Exception e) {
			_log.info("Abnormal login error. account=" + accountName + " host=" + host);
			client.kick();
			return;
		}
	}
	
	private void entered(GameClient client) {
		String accountName = client.getAccountName();
		// 檢查是否有任何需要閱讀的通知
		if (S_CommonNews.getNoticeCount(accountName) > 0) {
			S_CommonNews news = new S_CommonNews(accountName, client);
			client.sendPacket(news);
			news.clear();
			news = null;
			return;
		}
		new C_CharacterSelect(client);
		client.setLoginAvailable();
	}
	
	private boolean isValidAccount(String account) {
		if(account.length() < 5 || account.length() > 12)return false;
		char[] chars = account.toCharArray();
		for(int i = 0; i < chars.length; i++){
			if(!Character.isLetterOrDigit(chars[i]))
				return false;
		}
		return true;
	}

	private boolean isValidPassword(String password) {
		if(password.length() < 6 || password.length() > 16)return false;
		boolean hasLetter = false;
		boolean hasDigit = false;

		char[] chars = password.toCharArray();
		for(int i = 0; i < chars.length; i++){
			if(Character.isLetter(chars[i]))
				hasLetter = true;
			else if(Character.isDigit(chars[i]))
				hasDigit = true;
			else
				return false;
		}
		//if(!hasLetter || !hasDigit)
		//	return false;
		return true;
	}
}

