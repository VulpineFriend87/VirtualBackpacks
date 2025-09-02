package top.vulpine.virtualBackpacks.instance;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

public class Backpack implements InventoryHolder {

    @Getter
    private final UUID owner;

    @Getter
    private final Inventory inventory;

    public Backpack(UUID owner, ItemStack[] contents) {
        this.owner = owner;
        this.inventory = Bukkit.createInventory(this, 27, "Backpack");
        if (contents != null) this.inventory.setContents(contents);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public String serializeContents() throws Exception {

        ItemStack[] contents = inventory.getContents();

        return serializeContents(contents);

    }

    public static String serializeContents(ItemStack[] contents) throws Exception {

        List<Map<String, Object>> serialized = new ArrayList<>();
        for (ItemStack item : contents) {
            serialized.add(item != null ? item.serialize() : null);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(serialized);

        oos.close();

        return Base64.getEncoder().encodeToString(baos.toByteArray());

    }

    @SuppressWarnings("unchecked")
    public static ItemStack[] deserializeContents(String base64) throws Exception {

        if (base64 == null || base64.isEmpty()) return null;

        byte[] data = Base64.getDecoder().decode(base64);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));

        List<Map<String, Object>> serialized = (List<Map<String, Object>>) ois.readObject();

        ois.close();

        ItemStack[] items = new ItemStack[serialized.size()];
        for (int i = 0; i < serialized.size(); i++) {
            items[i] = serialized.get(i) != null ? ItemStack.deserialize(serialized.get(i)) : null;
        }

        return items;

    }
}
