package UtilitiesPKG;

import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SerializeItemStack {

	public SerializeItemStack() {
	}

	public static ItemStack item;
	public static ItemMeta itemM;

	@SuppressWarnings("deprecation")
	public static ItemStack deserealizeItem(String localItens) {
		try {
			localItens = localItens.replace(" ", "");
			// id:data,quantia,porcentagem,nome,idEnchant:level-idEnchant:level
			String[] args = localItens.split(";");

			if (args.length != 5) {
				return new ItemStack(Material.AIR);
			}

			int id = 0;
			int data = 0;

			if (args[0].contains(":")) {
				String[] argsID = args[0].split(":");
				id = Integer.parseInt(argsID[0]);
				data = Integer.parseInt(argsID[1]);
			} else {
				return new ItemStack(Material.AIR);

			}

			int qnt = Integer.parseInt(args[1]);
			int porcent = Integer.parseInt(args[2]);

			String nome = args[3].replace("_", " ");
			String encantamentos = args[4];

			Random r = new Random();
			int num = r.nextInt(100);
			if (num <= porcent) {
				item = new ItemStack(Material.getMaterial(id), qnt, (short) data);
			} else {
				item = new ItemStack(Material.AIR);
				return item;
			}

			if (!nome.equalsIgnoreCase("null") && !nome.equalsIgnoreCase("nada")) {
				itemM = item.getItemMeta();
				itemM.setDisplayName(nome.replace("&", "§"));
				item.setItemMeta(itemM);
			}

			if (!encantamentos.equalsIgnoreCase("null") && !encantamentos.equalsIgnoreCase("nada")) {
				String[] enchants = encantamentos.split("-");
				for (String enchant : enchants) {
					String[] leveis = enchant.split(":");
					item.addUnsafeEnchantment(Enchantment.getById(Integer.parseInt(leveis[0])),
							Integer.parseInt(leveis[1]));
				}
			}
			return item;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public static String serializeItemStack(ItemStack itemStack) {

		String id = itemStack.getTypeId() + "";
		String data = itemStack.getData().getData() + "";
		String amount = itemStack.getAmount() + "";

		String name = "";

		if (itemStack.hasItemMeta() && itemStack.getItemMeta().getDisplayName() != null) {
			name = itemStack.getItemMeta().getDisplayName();
		} else {
			name = "null";
		}

		String lineEnchants = "null";

		for (Entry<Enchantment, Integer> enchants : itemStack.getEnchantments().entrySet()) {
			if (lineEnchants == "null") {
				lineEnchants = enchants.getKey().getId() + ":" + enchants.getValue();
			} else {
				lineEnchants += "-" + enchants.getKey().getId() + ":" + enchants.getValue();
			}
		}

		String lineFinal = id + ":" + data + "; " + amount + "; " + name + "; " + lineEnchants;
		return lineFinal;
	}

	@SuppressWarnings({ "deprecation", "unused" })
	public static ItemStack deserializeItemStack(String line) {
		line = line.replace(" ", "");
		String[] args = line.split(";");

		if (args.length != 4) {
			return new ItemStack(Material.AIR);
		}

		String[] data = args[0].split(":");

		Integer id = Integer.valueOf(data[0]);
		Byte dataValue = Byte.valueOf(data[1]);

		Integer amount = Integer.valueOf(args[1]);

		ItemStack item = new ItemStack(id, amount, dataValue);

		if (item == null) {
			return new ItemStack(Material.AIR);
		}

		if (!(args[2].equalsIgnoreCase("null"))) {
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(args[2].replace("_", " "));
			item.setItemMeta(meta);
		}

		if (!(args[3].equalsIgnoreCase("null"))) {
			String[] enchants = args[3].split("-");

			for (String enchant : enchants) {
				String[] levels = enchant.split(":");
				item.addUnsafeEnchantment(Enchantment.getById(Integer.valueOf(levels[0])), Integer.valueOf(levels[1]));
			}
		}
		return item;
	}

}