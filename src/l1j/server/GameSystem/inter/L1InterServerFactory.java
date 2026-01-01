package l1j.server.GameSystem.inter;

import java.util.concurrent.ConcurrentHashMap;

import l1j.server.server.construct.L1InterServer;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Paralysis;
import l1j.server.server.serverpackets.inter.S_ConnectHibreedServer;

/**
 * InterServer Connection storage
 * @author LinOffice
 */
public class L1InterServerFactory {
	private static final ConcurrentHashMap<String, L1InterServerModel> INTER_DATA	= new ConcurrentHashMap<>();
	
	private L1InterServerFactory(){}

	public static boolean regist(final L1PcInstance pc, 
			final int interX, final int interY, final short interMapId, final int interHead, 
			final L1InterServer inter){
		if (INTER_DATA.contains(pc.getName())) {
			return false;
		}
		pc.getNetConnection().onInterServerLock();
		L1InterServerModel model = new L1InterServerModel(pc.getId(), pc.getName(), pc.getAccount(), interX, interY, interMapId, interHead, pc.getX(), pc.getY(), pc.getMapId(), pc, inter);
		INTER_DATA.put(pc.getName(), model);
		pc.sendPackets(S_Paralysis.RESTRICT_ON);
		pc.sendPackets(new S_ConnectHibreedServer(model), true);
		return true;
	}

	public static boolean contains(final String charName) {
		return INTER_DATA.containsKey(charName);
	}

	public static L1InterServerModel get(final String charName) {
		return INTER_DATA.get(charName);
	}

	public static void remove(final String charName) {
		if (!INTER_DATA.containsKey(charName)) {
			return;
		}
		INTER_DATA.remove(charName);
	}
}

