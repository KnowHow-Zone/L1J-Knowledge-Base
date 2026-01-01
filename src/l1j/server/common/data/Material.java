package l1j.server.common.data;

import java.util.Arrays;
import java.util.List;

public enum Material{
	NONE(			0,	"-", "-"),
	LIQUID(			1,	"液體", "Liquid"),
	WAX(			2,	"蠟", "Wax"),
	VEGGY(			3,	"植物性", "Vegetable"),
	FLESH(			4,	"動物性", "Animal"),
	PAPER(			5,	"紙", "Paper"),
	CLOTH(			6,	"布", "Cloth"),
	LEATHER(		7,	"皮", "Leather"), 
	WOOD(			8,	"木", "Wood"),
	BONE(			9,	"骨頭", "Bone"),
	DRAGON_HIDE(	10,	"龍鱗", "Dragon Hide"),
	IRON(			11,	"鐵", "Iron"),
	METAL(			12,	"金屬", "Metal"),
	COPPER(			13,	"銅", "Copper"),
	SILVER(			14,	"銀", "Silver"),
	GOLD(			15,	"金", "Gold"),
	PLATINUM(		16,	"白金", "Platinum"),
	MITHRIL(		17,	"米索莉", "Mithril"),
	PLASTIC(		18,	"黑色米索莉", "Black Mithril"),
	GLASS(			19,	"玻璃", "Glass"),
	GEMSTONE(		20,	"寶石", "Gemstone"),
	MINERAL(		21,	"礦石", "Mineral"),
	ORIHARUKON(		22,	"奧里哈魯根", "Oriharukon"),
	DRANIUM(		23,	"鋼鐵", "Dranium"),
	;
	private int value;
	private String name;
	private String name_en;
	Material(int val, String val2, String val3){
		value = val;
		name = val2;
		name_en = val3;
	}
	public int toInt(){
		return value;
	}
	public String toName() {
		return name;
	}
	public String toNameEn() {
		return name_en;
	}
	public boolean equals(Material v){
		return value == v.value;
	}
	public static Material fromInt(int i){
		switch(i){
		case 0:
			return NONE;
		case 1:
			return LIQUID;
		case 2:
			return WAX;
		case 3:
			return VEGGY;
		case 4:
			return FLESH;
		case 5:
			return PAPER;
		case 6:
			return CLOTH;
		case 7:
			return LEATHER;
		case 8:
			return WOOD;
		case 9:
			return BONE;
		case 10:
			return DRAGON_HIDE;
		case 11:
			return IRON;
		case 12:
			return METAL;
		case 13:
			return COPPER;
		case 14:
			return SILVER;
		case 15:
			return GOLD;
		case 16:
			return PLATINUM;
		case 17:
			return MITHRIL;
		case 18:
			return PLASTIC;
		case 19:
			return GLASS;
		case 20:
			return GEMSTONE;
		case 21:
			return MINERAL;
		case 22:
			return ORIHARUKON;
		case 23:
			return DRANIUM;
		default:
			throw new IllegalArgumentException(String.format("invalid arguments Material, %d", i));
		}
	}
	
	public static Material fromString(String str) {
		switch (str) {
		case "NONE":
			return NONE;
		case "LIQUID":
			return LIQUID;
		case "WAX":
			return WAX;
		case "VEGGY":
			return VEGGY;
		case "FLESH":
			return FLESH;
		case "PAPER":
			return PAPER;
		case "CLOTH":
			return CLOTH;
		case "LEATHER":
			return LEATHER;
		case "WOOD":
			return WOOD;
		case "BONE":
			return BONE;
		case "DRAGON_HIDE":
			return DRAGON_HIDE;
		case "IRON":
			return IRON;
		case "METAL":
			return METAL;
		case "COPPER":
			return COPPER;
		case "SILVER":
			return SILVER;
		case "GOLD":
			return GOLD;
		case "PLATINUM":
			return PLATINUM;
		case "MITHRIL":
			return MITHRIL;
		case "PLASTIC":
			return PLASTIC;
		case "GLASS":
			return GLASS;
		case "GEMSTONE":
			return GEMSTONE;
		case "MINERAL":
			return MINERAL;
		case "ORIHARUKON":
			return ORIHARUKON;
		case "DRANIUM":
			return DRANIUM;
		default:
			throw new IllegalArgumentException(String.format("invalid arguments Material, %s", str));
		}
	}
	
	private static final List<Material> UNDEAD_MATERIALS = Arrays.asList(
		new Material[] {
			SILVER, MITHRIL, ORIHARUKON
		}
	);
	private static final List<Material> NOT_SAFE_ENCHANT_MATERIALS = Arrays.asList(
		new Material[] {
			BONE, PLASTIC, DRANIUM
		}
	);
	
	public static boolean isUndeadMaterial(Material material){
		return UNDEAD_MATERIALS.contains(material);
	}
	
	public static boolean isNotSafeEnchantMaterial(Material material){
		return NOT_SAFE_ENCHANT_MATERIALS.contains(material);
	}
}

