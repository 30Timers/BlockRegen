package nl.aurorion.blockregen.compatibility.material;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.util.Key;
import nl.aurorion.blockregen.material.BlockRegenMaterial;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.Objects;

// Check using the CraftEngine API whether the destroyed block matches.
public class CraftEngineMaterial implements BlockRegenMaterial {

    @Getter
    private final String craftEngineID;

    public CraftEngineMaterial(String craftEngineID) {
        this.craftEngineID = craftEngineID;
    }

    @Override
    public boolean check(Block block) {
        if (!CraftEngineBlocks.isCustomBlock(block)) {
            return false;
        }
        String blockId = BukkitAdaptors.adapt(block).id().asString();
        return Objects.equals(blockId, this.craftEngineID);
    }

    @Override
    public void setType(Block block) {
        CraftEngineBlocks.place(block.getLocation(), Key.of(this.craftEngineID), false);
    }

    @Override
    public XMaterial getType() {
        if (CraftEngineBlocks.byId(Key.of(this.craftEngineID)) == null) {
            throw new IllegalArgumentException(String.format("Invalid CraftEngine material: %s", this.craftEngineID));
        }
        BlockData blockData = CraftEngineBlocks.getBukkitBlockData(CraftEngineBlocks.byId(Key.of(this.craftEngineID)).defaultState());
        return XMaterial.matchXMaterial(blockData.getMaterial());
    }

    @Override
    public String getConfigurationString() {
        return this.craftEngineID;
    }

    @Override
    public String toString() {
        return "OraxenMaterial{" +
                "oraxenId='" + craftEngineID + '\'' +
                '}';
    }
}
