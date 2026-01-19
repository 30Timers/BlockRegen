package nl.aurorion.blockregen.compatibility.provider;

import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.util.Key;
import nl.aurorion.blockregen.BlockRegenPlugin;
import nl.aurorion.blockregen.ParseException;
import nl.aurorion.blockregen.compatibility.CompatibilityProvider;
import nl.aurorion.blockregen.compatibility.material.CraftEngineMaterial;
import nl.aurorion.blockregen.conditional.Condition;
import nl.aurorion.blockregen.drop.ItemProvider;
import nl.aurorion.blockregen.material.BlockRegenMaterial;
import nl.aurorion.blockregen.material.MaterialProvider;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CraftEngineProvider extends CompatibilityProvider implements ItemProvider, MaterialProvider {

    public CraftEngineProvider(BlockRegenPlugin plugin) {
        super(plugin, "craftengine");
        setFeatures("materials", "drops", "conditions");
    }

    @Override
    public void onLoad() {
        // Register conditions provider.
        plugin.getPresetManager().getConditions().addProvider(getPrefix() + "/tool", ((key, node) -> {
            String id = (String) node;

            if (CraftEngineItems.byId(Key.of(id)) == null) {
                throw new ParseException("Invalid CraftEngine item '" + id + "'");
            }

            return Condition.of((ctx) -> {
                ItemStack tool = (ItemStack) ctx.mustVar("tool");
                String toolId = CraftEngineItems.getCustomItemId(tool).asString();
                return id.equals(toolId);
            });
        }));
    }

    /**
     * @throws ParseException If the parsing fails.
     */
    @Override
    public @NotNull BlockRegenMaterial parseMaterial(String input) {
        if (CraftEngineBlocks.byId(Key.from(input)) == null) {
            throw new ParseException(String.format("'%s' is not a CraftEngine block.", input));
        }
        return new CraftEngineMaterial(input);
    }

    @Override
    public @Nullable BlockRegenMaterial load(@NonNull Block block) {
        ImmutableBlockState customBlockState = CraftEngineBlocks.getCustomBlockState(block);
        if (customBlockState == null) {
            return null;
        }
        return new CraftEngineMaterial(customBlockState.customBlockState().ownerId().asString());
    }

    @Override
    public @NonNull Class<?> getClazz() {
        return CraftEngineMaterial.class;
    }

    @Override
    public ItemStack createItem(@NonNull String id, @NonNull Function<String, String> parser, int amount) {
        ItemStack item = CraftEngineItems.byId(Key.of(id)).buildItemStack();
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(parser.apply(meta.getDisplayName()));
        meta.setLore(meta.getLore().stream().map(parser).collect(Collectors.toList()));
        item.setItemMeta(meta);
        item.setAmount(amount);
        return item;
    }

    @Override
    public boolean exists(@NonNull String id) {
        return CraftEngineItems.byId(Key.of(id)) != null;
    }

    @Override
    public BlockRegenMaterial createInstance(Type type) {
        return new CraftEngineMaterial(null);
    }

    @Override
    public boolean containsColon() {
        return true;
    }
}
