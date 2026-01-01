package l1j.server.server.command.executor;

import java.util.StringTokenizer;

import l1j.server.server.datatables.HarddriveTable;
import l1j.server.server.datatables.HarddriveTable.BanHarddrive;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.message.S_ServerMessage;
import l1j.server.server.serverpackets.message.S_SystemMessage;
import l1j.server.server.utils.StringUtil;

public class L1HarddriveBan implements L1CommandExecutor {
	private static class newInstance {
		public static final L1CommandExecutor INSTANCE = new L1HarddriveBan();
	}
	public static L1CommandExecutor getInstance() {
		return newInstance.INSTANCE;
	}
	private L1HarddriveBan() {}

	@Override
	public boolean execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer st	= new StringTokenizer(arg);
			String type			= st.nextToken();
			
			switch(type){
			case "add":
				String addParam		= st.nextToken();
				L1PcInstance target	= L1World.getInstance().getPlayer(addParam);
				if (target == null || target.getNetConnection() == null) {
					pc.sendPackets(new S_ServerMessage(S_ServerMessage.getStringIdx(47), addParam), true);
					return false;
				}
				String[] loginInfo = target.getNetConnection().getLoginInfo();
				if (loginInfo == null) {
					pc.sendPackets(new S_ServerMessage(S_ServerMessage.getStringIdx(58), addParam), true);
					return false;
				}
				/***
				if (StringUtil.isNullOrEmpty(session.getHddId())) {
					pc.sendPackets(new S_ServerMessage(S_ServerMessage.getStringIdx(59), addParam), true);
					return false;
				}
				HarddriveTable.getInstance().insert(session.getHddId(), session.getAccount());
				***/
				target.getNetConnection().kick();
				pc.sendPackets(new S_ServerMessage(S_ServerMessage.getStringIdx(60), addParam), true);
				return true;
			case "delete":
				String delParam		= st.nextToken();
				HarddriveTable.getInstance().deleteAccount(delParam);
				pc.sendPackets(new S_ServerMessage(S_ServerMessage.getStringIdx(61), delParam), true);
				return true;
			case "reload":
				HarddriveTable.getInstance().reload();
				pc.sendPackets(new S_SystemMessage(S_SystemMessage.getRefText(452), true), true);
				return true;
			default:
				String param		= st.nextToken();
				BanHarddrive ban = HarddriveTable.getHardDiskBan(param);
				if (ban == null) {
					pc.sendPackets(new S_ServerMessage(S_ServerMessage.getStringIdx(62), param), true);
					return false;
				}
				pc.sendPackets(new S_SystemMessage(ban.toString()), true);
				return true;
			}
			
		} catch (Exception exception) {
			pc.sendPackets(new S_ServerMessage(S_ServerMessage.getStringIdx(63), cmdName), true);
			return false;
		}
	}
}


